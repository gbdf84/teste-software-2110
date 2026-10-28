package com.gilberto.testesoftware2110;

import com.gilberto.testesoftware2110.dto.UsuarioRequest;
import com.gilberto.testesoftware2110.dto.UsuarioResponse;
import com.gilberto.testesoftware2110.reporitory.UsuariosRepository;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.security.SecureRandom;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Testes REST da API de usuários")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsuariosRestAssuredIntegrationTest {
    private static final String BASE_PATH = "/api/usuarios";
    private static final SecureRandom RANDOM = new SecureRandom();

    @LocalServerPort
    private int port;

    @Autowired
    private UsuariosRepository repository;

    private String cpfCriado;
    private String cpfParaExcluir;
    private UUID usuarioId;

    @BeforeAll
    void configurarRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        cpfCriado = gerarCpfValido();
        cpfParaExcluir = gerarCpfValido();
    }

    @AfterAll
    void limparUsuarioCriadoPeloTeste() {
        if (cpfCriado != null) {
            repository.findByCpf(cpfCriado).ifPresent(repository::delete);
        }
        if (cpfParaExcluir != null) {
            repository.findByCpf(cpfParaExcluir).ifPresent(repository::delete);
        }
        RestAssured.reset();
    }

    @Test
    @DisplayName("GET lista usuários e retorna a coleção")
    void deveListarUsuarios() {
        given()
            .log().ifValidationFails()
        .when()
            .get(BASE_PATH)
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("POST cria usuário e GET consulta os dados por UUID")
    void deveCriarEConsultarUsuario() {
        UsuarioRequest novoUsuario = new UsuarioRequest(
                cpfCriado,
                "Usuario Rest Assured",
                LocalDate.of(1995, 5, 20),
                "SenhaSegura123"
        );

        Response respostaCriacao = given()
                .contentType("application/json")
                .body(novoUsuario)
                .log().ifValidationFails()
            .when()
                .post(BASE_PATH)
            .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("id", notNullValue())
                .body("cpf", equalTo(cpfCriado))
                .body("nome", equalTo("Usuario Rest Assured"))
                .extract()
                .response();

        UsuarioResponse criado = respostaCriacao.as(UsuarioResponse.class);
        usuarioId = criado.id();
        assertNotNull(usuarioId);

        given()
            .pathParam("id", usuarioId)
            .log().ifValidationFails()
        .when()
            .get(BASE_PATH + "/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(usuarioId.toString()))
            .body("nome", equalTo("Usuario Rest Assured"))
            .body("cpf", equalTo(cpfCriado));
    }

    @Test
    @DisplayName("DELETE remove usuário criado pela API")
    void deveExcluirUsuario() {
        UsuarioRequest novoUsuario = new UsuarioRequest(
                cpfParaExcluir,
                "Usuario Para Excluir",
                LocalDate.of(1992, 8, 15),
                "SenhaSegura123"
        );

        String id = given()
                .contentType("application/json")
                .body(novoUsuario)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
            .pathParam("id", id)
            .log().ifValidationFails()
        .when()
            .delete(BASE_PATH + "/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(204);

        assertFalse(repository.existsByCpf(cpfParaExcluir));
    }

    private static String gerarCpfValido() {
        StringBuilder numeros = new StringBuilder(9);
        for (int i = 0; i < 9; i++) {
            numeros.append(RANDOM.nextInt(10));
        }
        String base = numeros.toString();
        if (base.chars().distinct().count() == 1) {
            return gerarCpfValido();
        }
        int primeiroDigito = calcularDigito(base, 10);
        int segundoDigito = calcularDigito(base + primeiroDigito, 11);
        return base + primeiroDigito + segundoDigito;
    }

    private static int calcularDigito(String numeros, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < numeros.length(); i++) {
            soma += Character.digit(numeros.charAt(i), 10) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
