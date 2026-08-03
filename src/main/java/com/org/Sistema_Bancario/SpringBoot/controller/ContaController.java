package com.org.Sistema_Bancario.SpringBoot.controller;

import com.org.Sistema_Bancario.SpringBoot.config.SecurityConfig;
import com.org.Sistema_Bancario.SpringBoot.dto.ApiError;
import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginResponse;

import com.org.Sistema_Bancario.SpringBoot.dto.MensagemResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.SaldoResponse;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
@Tag(name = "conta", description = "Controlador para criar e acessar contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/criarConta")
    @Operation(summary = "Cria conta do cliente",
            description = "Método que cria uma conta corrente e uma investimento para o cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado",
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
    public ResponseEntity<MensagemResponse> cadastroDaConta(
           @Valid @RequestBody CadastroRequest cadastro) {
            contaService.criarConta(cadastro);
            return new ResponseEntity<>(new MensagemResponse("Conta criada com sucesso!"), HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Acessa a conta do cliente", description = "Método para realizar o login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
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
    public ResponseEntity<LoginResponse> acessarConta(
           @Valid @RequestBody LoginRequest login) {
        String token = contaService.verify(login);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/contas")
    @SecurityRequirement(name = SecurityConfig.SECURITY)
    @Operation(summary = "Lista as contas do cliente", description = "Método que retorna uma lista de contas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                            schema = @Schema(implementation = ContaResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Contas não encontradas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
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
    public ResponseEntity<List<ContaResponse>> listarContas(Authentication auth){
        String cpf = auth.getName();
        return new ResponseEntity<>(contaService.listarContas(cpf), HttpStatus.OK);
    }

    @GetMapping("/conta/saldo")
    @SecurityRequirement(name = SecurityConfig.SECURITY)
    @Operation(summary = "Mostra o saldo da conta escolhida",
            description = "Método que retorna o saldo da conta corrente ou investimento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação feita com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SaldoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada/Aplicação não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
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
    public ResponseEntity<SaldoResponse> saldoConta(Authentication auth,
                                                 @Parameter(
                                                         description = "Tipo da conta",
                                                         example = "CORRENTE"
                                                 )
                                                 @RequestParam TipoConta tipo){
        String cpf = auth.getName();
        return new ResponseEntity<>(new SaldoResponse(contaService.saldoConta(cpf,tipo)),HttpStatus.OK);
    }


}
