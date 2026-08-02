package com.org.Sistema_Bancario.SpringBoot.e2e;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginResponse;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthE2ETest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }
    @Test
    void deveRetornarTokenAoRealizarLogin() {
        // given, when and then
        Cliente cliente = new Cliente("Patrício Silva Rodriguete",
                "11144477735",
                "204689");
        CadastroRequest cadastroRequest = new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getPassword()
        );
        LoginRequest loginRequest = new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
        given().contentType(ContentType.JSON).body(cadastroRequest)
                .when().post("/api/criarConta")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        given().contentType(ContentType.JSON).body(loginRequest)
                .when().post("/api/auth/login")
                .then().statusCode(HttpStatus.OK.value())
                .extract().body().as(LoginResponse.class);
    }
}
