package com.org.Sistema_Bancario.SpringBoot.controller;

import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.TransferenciaRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ValorRequest;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.service.ExtratoService;
import com.org.Sistema_Bancario.SpringBoot.service.MovimentacaoService;
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
public class MovimentacaoController {

    private final ExtratoService extratoService;
    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService,
                                  ExtratoService extratoService) {
        this.movimentacaoService = movimentacaoService;
        this.extratoService = extratoService;
    }

    @GetMapping("/contas/verExtrato")
    public ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(Authentication auth){
        String cpf = auth.getName();
        return new ResponseEntity<>(extratoService.verExtrato(cpf),HttpStatus.OK);
    }

    @PostMapping("/contas/saque")
    public ResponseEntity<String> saque(Authentication auth,
                                        @RequestBody ValorRequest request){
        String cpf = auth.getName();
        movimentacaoService.realizarMovimentacao(cpf,request.valor(),TipoMovimentacao.SAQUE);
        return new ResponseEntity<>("Saque realizado com sucesso!",HttpStatus.OK);
    }
    @PostMapping("/contas/deposito")
    public ResponseEntity<String> deposito(Authentication auth,
                           @RequestBody ValorRequest request){
        String cpf = auth.getName();
        movimentacaoService.realizarMovimentacao(cpf,request.valor(),TipoMovimentacao.DEPOSITO);
        return new ResponseEntity<>("Depósito realizado com sucesso!",HttpStatus.OK);
    }
    @PostMapping("/transferencia")
    public ResponseEntity<String> transferencia(Authentication auth,
                                @RequestBody TransferenciaRequest request){
        String origem = auth.getName();
        System.out.println("origem: "+origem+"\n"+
                "destino: " +request.destino()+"\n"+
                "valor: "+request.valor()+"\n"+
                "tipo: "+request.tipo());
        var response =
                new ResponseEntity<>("Transferencia realizada com sucesso!",HttpStatus.OK);
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
