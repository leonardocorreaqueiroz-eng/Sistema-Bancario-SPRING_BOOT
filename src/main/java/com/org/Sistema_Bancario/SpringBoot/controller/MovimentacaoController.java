package com.org.Sistema_Bancario.SpringBoot.controller;

import com.org.Sistema_Bancario.SpringBoot.config.SecurityConfig;
import com.org.Sistema_Bancario.SpringBoot.dto.ApiError;
import com.org.Sistema_Bancario.SpringBoot.dto.MensagemResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.TransferenciaRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ValorRequest;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.service.ExtratoService;
import com.org.Sistema_Bancario.SpringBoot.service.MovimentacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(
        name = "movimentação",
        description = "Operações de depósito, saque, transferência e extrato"
)
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class MovimentacaoController {

    private final ExtratoService extratoService;
    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService,
                                  ExtratoService extratoService) {
        this.movimentacaoService = movimentacaoService;
        this.extratoService = extratoService;
    }

    @GetMapping("/contas/verExtrato")
    @Operation(summary = "Exibe o extrato",
            description = "Método que exibe o extrato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato encontrado com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                schema = @Schema(implementation = MovimentacaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Extrato vazio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    ))
    })
    public ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(Authentication auth){
        String cpf = auth.getName();
        return new ResponseEntity<>(extratoService.verExtrato(cpf),HttpStatus.OK);
    }

    @PostMapping("/contas/saque")
    @Operation(summary = "Realiza o saque da conta corrente",
            description = "Método que realiza o saque da conta corrente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    ))
    })
    public ResponseEntity<MensagemResponse> saque(Authentication auth,
                                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                          description = "Valor do saque",
                                                          required = true,
                                                          content = @Content(
                                                                  schema = @Schema(implementation = ValorRequest.class)
                                                          )
                                                  )
                                                  @Valid @RequestBody ValorRequest request){
        String cpf = auth.getName();
        movimentacaoService.realizarMovimentacao(cpf,request.valor(),TipoMovimentacao.SAQUE);
        return new ResponseEntity<>(new MensagemResponse("Saque realizado com sucesso!"),HttpStatus.OK);
    }
    @PostMapping("/contas/deposito")
    @Operation(summary = "Realiza o depósito na conta corrente",
            description = "Método que realiza o depósito na conta corrente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    ))
    })
    public ResponseEntity<MensagemResponse> deposito(Authentication auth,
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                             description = "Valor do depósito",
                                                             required = true,
                                                             content = @Content(
                                                                     schema = @Schema(implementation = ValorRequest.class)
                                                             )
                                                     )
                                                     @Valid @RequestBody ValorRequest request){
        String cpf = auth.getName();
        movimentacaoService.realizarMovimentacao(cpf,request.valor(),TipoMovimentacao.DEPOSITO);
        return new ResponseEntity<>(new MensagemResponse("Depósito realizado com sucesso!"),HttpStatus.OK);
    }
    @PostMapping("/transferencia")
    @Operation(summary = "Realiza uma transferência entre contas",
            description = "Método que realiza uma transferência entre contas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    ))
    })
    public ResponseEntity<MensagemResponse> transferencia(Authentication auth,
                                                          @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                  description = "Dados da transferência",
                                                                  required = true,
                                                                  content = @Content(
                                                                          schema = @Schema(implementation = TransferenciaRequest.class)
                                                                  )
                                                          )
                                                          @Valid @RequestBody TransferenciaRequest request){
        String origem = auth.getName();
        var response =
                new ResponseEntity<>(new MensagemResponse("Transferencia realizada com sucesso!"),HttpStatus.OK);
        if (origem.equals(request.destino()) && request.tipo() == TipoMovimentacao.APLICACAO){
            movimentacaoService.transferir(origem,
                    request.destino(),
                    request.valor(),
                    request.tipo(),
                    TipoConta.CORRENTE,TipoConta.INVESTIMENTO);
            return response;
        }
        if (origem.equals(request.destino()) && request.tipo() == TipoMovimentacao.RESGATE){
            movimentacaoService.transferir(origem,
                    request.destino(),
                    request.valor(),
                    request.tipo(),
                    TipoConta.INVESTIMENTO,TipoConta.CORRENTE);
            return response;
        }
        movimentacaoService.transferir(origem,
                request.destino(),
                request.valor(),
                request.tipo(),
                TipoConta.CORRENTE,TipoConta.CORRENTE);
        return response;
    }
}
