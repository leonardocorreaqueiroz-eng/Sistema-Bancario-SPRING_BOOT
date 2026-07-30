package com.org.Sistema_Bancario.SpringBoot.controller;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginResponse;

import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
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

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> acessarConta(
           @Valid @RequestBody LoginRequest login) {
        String token = contaService.verify(login);
        return ResponseEntity.ok(new LoginResponse(token));
    }
    @PostMapping("/criarConta")
    public ResponseEntity<String> cadastroDaConta(
           @Valid @RequestBody CadastroRequest cadastro) {
            contaService.criarConta(cadastro);
            return new ResponseEntity<>("Conta criada com sucesso!", HttpStatus.CREATED);
    }

    @GetMapping("/contas")
    public ResponseEntity<List<ContaResponse>> listarContas(Authentication auth){
        String cpf = auth.getName();
        return new ResponseEntity<>(contaService.listarContas(cpf), HttpStatus.OK);
    }

    @GetMapping("/conta/saldo")
    public ResponseEntity<BigDecimal> saldoConta(Authentication auth,
                                 @RequestParam TipoConta tipo){
        String cpf = auth.getName();
        return new ResponseEntity<>(contaService.saldoConta(cpf,tipo),HttpStatus.OK);
    }


}
