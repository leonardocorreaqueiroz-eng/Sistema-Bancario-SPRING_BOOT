package com.org.Sistema_Bancario.SpringBoot.controller;

import com.org.Sistema_Bancario.SpringBoot.dto.ApiError;
import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ClienteJaCadastradoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaInativaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ExtratoVazioException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.MovimentacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.OperacaoNaoPermitidaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.SaldoInsuficienteException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ValorInvalidoException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAplicacaoException(Exception e){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.warn("Erro de aplicação: {}", e.getMessage());
        return buildResponse(status, e.getMessage());
    }

    @ExceptionHandler(AplicacaoException.class)
    public ResponseEntity<ApiError> handleAplicacaoException(AplicacaoException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Erro de aplicação: {}", e.getMessage());
        return buildResponse(status, e.getMessage());
    }

    private static @NonNull ResponseEntity<ApiError> buildResponse(HttpStatus status, String e) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                e
        );
        return ResponseEntity.status(status).body(error);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(BadCredentialsException ex) {
        log.warn("CPF ou senha inválidos");

        return buildResponse(HttpStatus.UNAUTHORIZED,"CPF ou senha inválidos");
    }

    @ExceptionHandler(AplicacaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleAplicacaoNaoEcontradaException(AplicacaoNaoEncontradaException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ClienteJaCadastradoException.class)
    public ResponseEntity<ApiError> handleClienteJaCadastradoException(ClienteJaCadastradoException e){
        HttpStatus status = HttpStatus.CONFLICT;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ContaException.class)
    public ResponseEntity<ApiError> handleContaException(ContaException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ContaInativaException.class)
    public ResponseEntity<ApiError> handleContaInativaException(ContaInativaException e){
        HttpStatus status = HttpStatus.FORBIDDEN;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(MovimentacaoException.class)
    public ResponseEntity<ApiError> handleMovimentacaoException(MovimentacaoException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Dados inválidos");

        log.warn("Erro de validação: {}", mensagem);

        return buildResponse(HttpStatus.BAD_REQUEST, mensagem);
    }
    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<ApiError> handleOperacaoNaoPermitidaException(OperacaoNaoPermitidaException e){
        HttpStatus status = HttpStatus.FORBIDDEN;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ApiError> handleSaldoInsuficienteException(SaldoInsuficienteException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Saldo insuficiente: {}", e.getMessage());
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<ApiError> handleValorInvalidoException(ValorInvalidoException e){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleContaNaoEncontradaException(ContaNaoEncontradaException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Conta não encontrada: {}", e.getMessage());
        return buildResponse(status, e.getMessage());
    }
    @ExceptionHandler(ExtratoVazioException.class)
    public ResponseEntity<ApiError> handleExtratoVazioException(ExtratoVazioException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, e.getMessage());
    }

}
