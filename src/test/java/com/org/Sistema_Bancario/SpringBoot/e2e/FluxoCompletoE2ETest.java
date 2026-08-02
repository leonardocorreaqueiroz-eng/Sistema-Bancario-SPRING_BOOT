package com.org.Sistema_Bancario.SpringBoot.e2e;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.TransferenciaRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ValorRequest;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FluxoCompletoE2ETest {
    @LocalServerPort
    private int port;
    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }
    @Test
    void fluxoCompletoDeMovimentacao(){
        Cliente cliente = getCliente();
        CadastroRequest cadastroRequest = getCadastroRequest(cliente);
        LoginRequest loginRequest = getLoginRequest(cliente);

        given().contentType(ContentType.JSON).body(cadastroRequest)
                .when()
                .post("/api/criarConta")
                .then().statusCode(HttpStatus.CREATED.value())
                .body(equalTo("Conta criada com sucesso!"));


        String token = getToken(loginRequest);

        List<ContaResponse> contas = given().header("Authorization", "Bearer " + token)
                .when().get("/api/contas")
                .then().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("$");

        var numeroDeContas = 2;

        assertEquals(numeroDeContas, contas.size());

        var valorDeDeposito = new ValorRequest(new BigDecimal("4000"));

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(valorDeDeposito)
                .when().post("/api/contas/deposito")
                .then().statusCode(HttpStatus.OK.value());

        var saldo = getBigDecimal(token,TipoConta.CORRENTE);

        assertEquals(0, saldo.compareTo(valorDeDeposito.valor()));

        var valorDeSaque = new ValorRequest(new BigDecimal("2000.47"));

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(valorDeSaque)
                .when().post("/api/contas/saque")
                .then().statusCode(HttpStatus.OK.value());

        BigDecimal resultado = saldo.subtract(valorDeSaque.valor()
                .add(valorDeSaque.valor().multiply(RegrasDeBanco.TAXA_SAQUE)));

        var extrato = getExtrato(token);

        var movimentacoesEsperadas = 2;

        var saldo1  = getBigDecimal(token,TipoConta.CORRENTE);
        assertAll(
                () -> assertEquals(resultado.setScale(2, RoundingMode.HALF_EVEN), saldo1),
                () -> assertEquals(movimentacoesEsperadas,extrato.size())
        );



    }

    private static String getToken(LoginRequest loginRequest) {
        return given().contentType(ContentType.JSON).body(loginRequest)
                .when().post("/api/auth/login")
                .then().statusCode(HttpStatus.OK.value())
                .extract().body().as(LoginResponse.class).token();
    }

    private static @NonNull LoginRequest getLoginRequest(Cliente cliente) {
        return new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
    }

    private static @NonNull CadastroRequest getCadastroRequest(Cliente cliente) {
        return new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getPassword()
        );
    }

    private static @NonNull Cliente getCliente() {
        return new Cliente("Patrício Silva Rodriguete",
                "12345678909",
                "192292");
    }

    @Test
    void fluxoCompletoDeTransferencia(){
        Cliente cliente = getCliente();
        Cliente cliente2 = new Cliente("Mariana Conceição",
                "11144477735",
                "204689");
        CadastroRequest cadastroRequest = getCadastroRequest(cliente);
        LoginRequest loginRequest = getLoginRequest(cliente);
        CadastroRequest cadastroRequest2 = getCadastroRequest(cliente2);
        LoginRequest loginRequest2 = getLoginRequest(cliente2);

        given().contentType(ContentType.JSON).body(cadastroRequest)
                .when().post("/api/criarConta")
                .then().statusCode(HttpStatus.CREATED.value())
                .body(equalTo("Conta criada com sucesso!"));

        var tokenConta1 = getToken(loginRequest);

        given().contentType(ContentType.JSON).body(cadastroRequest2)
                .when().post("/api/criarConta")
                .then().statusCode(HttpStatus.CREATED.value())
                .body(equalTo("Conta criada com sucesso!"));

        var tokenConta2 = getToken(loginRequest2);

        var deposito = new ValorRequest(new BigDecimal("20000"));
        given().header("Authorization", "Bearer " + tokenConta1)
                .contentType(ContentType.JSON).body(deposito)
                .when().post("/api/contas/deposito")
                .then().statusCode(HttpStatus.OK.value());

        var valor = new BigDecimal("2000.47");

        TransferenciaRequest transferenciaRequest = new TransferenciaRequest(
                cliente2.getCpf(),
                valor,
                TipoMovimentacao.DOC
        );

        given().header("Authorization", "Bearer " + tokenConta1)
                .contentType(ContentType.JSON).body(transferenciaRequest)
                .when().post("/api/transferencia")
                .then().statusCode(HttpStatus.OK.value());

        var saldo1 = getBigDecimal(tokenConta1, TipoConta.CORRENTE);

        var saldo2 = getBigDecimal(tokenConta2, TipoConta.CORRENTE);

        var extrato1 = getExtrato(tokenConta1);

        var extrato2 = getExtrato(tokenConta2);

        var movimentacoesEsperadasConta1 = 2;
        var movimentacoesEsperadasConta2 = 1;

        assertAll(
                () -> assertEquals(deposito.valor().subtract(valor), saldo1),
                () -> assertEquals(valor, saldo2),
                () -> assertEquals(movimentacoesEsperadasConta1, extrato1.size()),
                () -> assertEquals(movimentacoesEsperadasConta2, extrato2.size())
        );
    }
    @Test
    void fluxoCompletoDeInvestimento() {
        Cliente cliente = getCliente();
        CadastroRequest cadastroRequest = getCadastroRequest(cliente);
        LoginRequest loginRequest = getLoginRequest(cliente);

        given().contentType(ContentType.JSON).body(cadastroRequest)
                .when().post("/api/criarConta")
                .then().statusCode(HttpStatus.CREATED.value())
                .body(equalTo("Conta criada com sucesso!"));

        var tokenConta1 = getToken(loginRequest);

        var deposito = new ValorRequest(new BigDecimal("20000"));

        given().header("Authorization", "Bearer " + tokenConta1)
                .contentType(ContentType.JSON).body(deposito)
                .when().post("/api/contas/deposito")
                .then().statusCode(HttpStatus.OK.value());

        var valor = new BigDecimal("10000.80");
        var aplicacaoRequest = new TransferenciaRequest(
                cliente.getCpf(),
                valor,
                TipoMovimentacao.APLICACAO
        );

        given().header("Authorization", "Bearer " + tokenConta1)
                .contentType(ContentType.JSON).body(aplicacaoRequest)
                .when().post("/api/transferencia")
                .then().statusCode(HttpStatus.OK.value());

        var saldoContaCorrente = getBigDecimal(tokenConta1, TipoConta.CORRENTE);

        var saldoContaInvestimento = getBigDecimal(tokenConta1, TipoConta.INVESTIMENTO);

        var extrato = getExtrato(tokenConta1);

        var movimentacoesEsperadas = 4;

        assertAll(
                () -> assertEquals(movimentacoesEsperadas, extrato.size()),
                () -> assertEquals(deposito.valor().subtract(valor)
                                .setScale(2, RoundingMode.UNNECESSARY), saldoContaCorrente),
                () -> assertEquals(valor.setScale(2,RoundingMode.UNNECESSARY), saldoContaInvestimento)
        );

        var resgate = new BigDecimal("1800");

        var resgateRequest = new TransferenciaRequest(
                cliente.getCpf(),
                resgate,
                TipoMovimentacao.RESGATE
        );

        given().header("Authorization", "Bearer " + tokenConta1)
                .contentType(ContentType.JSON).body(resgateRequest)
                .when().post("/api/transferencia")
                .then().statusCode(HttpStatus.OK.value());


        var saldoContaCorrente1 = getBigDecimal(tokenConta1, TipoConta.CORRENTE);

        var saldoContaInvestimento1 = getBigDecimal(tokenConta1, TipoConta.INVESTIMENTO);

        var extrato1 = getExtrato(tokenConta1);

        var movimentacoesEsperadas1 = 6;

        assertAll(
                () -> assertEquals(movimentacoesEsperadas1, extrato1.size()),
                () -> assertEquals(deposito.valor().subtract(valor).add(resgate)
                                .setScale(2, RoundingMode.UNNECESSARY), saldoContaCorrente1),
                () -> assertEquals(valor.subtract(resgate).setScale(2,RoundingMode.UNNECESSARY), saldoContaInvestimento1)
        );

    }

    private static List<MovimentacaoResponse> getExtrato(String tokenConta1) {
        return given().header("Authorization", "Bearer " + tokenConta1)
                .when().get("/api/contas/verExtrato")
                .then().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("$");
    }

    private static BigDecimal getBigDecimal(String tokenConta1, TipoConta corrente) {
        return given().header("Authorization", "Bearer " + tokenConta1)
                .queryParam("tipo", corrente)
                .when().get("/api/conta/saldo")
                .then().statusCode(HttpStatus.OK.value())
                .extract().as(BigDecimal.class);
    }
}
