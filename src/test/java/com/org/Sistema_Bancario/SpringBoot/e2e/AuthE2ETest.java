package com.org.Sistema_Bancario.SpringBoot.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthE2ETest {
    @LocalServerPort
    private int port;
    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }
    @Test
    void login() {
        // given, when and then
    }
}
