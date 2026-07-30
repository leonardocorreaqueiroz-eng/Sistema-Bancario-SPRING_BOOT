package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.config.JwtProperties;
import com.org.Sistema_Bancario.SpringBoot.model.ClienteDetails;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        // chave com 256 bits em Base64
        String secret = Base64.getEncoder().encodeToString(
                "12345678901234567890123456789012".getBytes()
        );

        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getExpiration()).thenReturn(60_000L); // 1 minuto
    }

    @Test
    void generateTokenDeveGerarUmJWT() {
        String token = jwtService.generateToken("12345678909");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUserNameDeveRetornarOCpf() {
        String token = jwtService.generateToken("12345678909");

        String username = jwtService.extractUserName(token);

        assertEquals("12345678909", username);
    }

    @Test
    void validateTokenDeveRetornarTrueQuandoTokenForValido() {
        Cliente cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292"
        );

        ClienteDetails userDetails = new ClienteDetails(cliente);

        String token = jwtService.generateToken(cliente.getCpf());

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateTokenDeveRetornarFalseQuandoUsuarioForDiferente() {

        Cliente cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "11111111111",
                "192292"
        );

        ClienteDetails userDetails = new ClienteDetails(cliente);

        String token = jwtService.generateToken("52998224725");

        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateTokenDeveRetornarFalseQuandoTokenEstiverExpirado() {

        when(jwtProperties.getExpiration()).thenReturn(-1000L);

        Cliente cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292"
        );

        ClienteDetails userDetails = new ClienteDetails(cliente);

        String token = jwtService.generateToken(cliente.getCpf());

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.validateToken(token, userDetails)
        );    }
}