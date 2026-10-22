package com.gilberto.testesoftware2110;

import com.gilberto.testesoftware2110.entity.Usuarios;
import com.gilberto.testesoftware2110.reporitory.UsuariosRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsuariosSeleniumIntegrationTest {
    private static final String CPF_FIXO_DE_TESTE = "52998224725";

    @LocalServerPort
    private int port;

    @Autowired
    private UsuariosRepository repository;

    private WebDriver driver;
    private WebDriverWait wait;
    private String cpfDeTeste;
    private boolean registroCriadoPelaSuite;

    @BeforeAll
    void prepararNavegadorEDadoDeTeste() {
        cpfDeTeste = CPF_FIXO_DE_TESTE;
        if (repository.existsByCpf(cpfDeTeste)) {
            throw new IllegalStateException("O CPF fixo de teste já está cadastrado; remova o registro de teste antes de executar a suíte.");
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--new-window", "--start-maximized", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void manterTelaVisivelEntreAsEtapas() {
        if (driver != null) pausaEntreAcoes();
    }

    @AfterAll
    void limparDadosDeTesteEFecharNavegador() {
        if (driver != null) {
            pausaEntreAcoes();
            driver.quit();
        }
        if (registroCriadoPelaSuite) {
            repository.findByCpf(cpfDeTeste).ifPresent(repository::delete);
        }
    }

    @Test
    @Order(1)
    void deveInserirUsuarioPelaTela() {
        assertFalse(repository.existsByCpf(cpfDeTeste), "CPF de teste deve estar livre antes da inserção");
        abrirTela();
        preencher("Usuário Selenium", cpfDeTeste, LocalDate.now().minusYears(30), "SenhaSegura123");
        driver.findElement(By.id("submit-button")).click();
        registroCriadoPelaSuite = true;
        pausaEntreAcoes();

        wait.until(d -> d.findElement(linhaDoCpf()).getText().contains("Usuário Selenium"));
        Usuarios salvo = repository.findByCpf(cpfDeTeste).orElseThrow();
        assertEquals("Usuário Selenium", salvo.getNome());
    }

    @Test
    @Order(2)
    void deveEditarUsuarioPelaTelaSemAlterarCpf() {
        Usuarios existente = repository.findByCpf(cpfDeTeste).orElseThrow();
        assertEquals("Usuário Selenium", existente.getNome(), "A edição deve usar o registro inserido no primeiro teste");
        abrirTela();
        WebElement linha = wait.until(ExpectedConditions.visibilityOfElementLocated(linhaDoCpf()));
        linha.findElement(By.cssSelector("button[aria-label^='Editar']")).click();
        pausaEntreAcoes();

        WebElement campoCpf = driver.findElement(By.id("cpf"));
        assertTrue(campoCpf.getAttribute("readonly") != null);
        WebElement nome = driver.findElement(By.id("nome"));
        nome.clear();
        pausaEntreAcoes();
        nome.sendKeys("Nome alterado");
        pausaEntreAcoes();
        driver.findElement(By.id("senha")).sendKeys("SenhaAtualizada123");
        pausaEntreAcoes();
        driver.findElement(By.id("submit-button")).click();
        pausaEntreAcoes();

        wait.until(d -> d.findElement(linhaDoCpf()).getText().contains("Nome alterado"));
        Usuarios atualizado = repository.findByCpf(cpfDeTeste).orElseThrow();
        assertEquals("Nome alterado", atualizado.getNome());
        assertEquals(cpfDeTeste, atualizado.getCpf());
    }

    @Test
    @Order(3)
    void deveExcluirUsuarioPelaTela() {
        Usuarios existente = repository.findByCpf(cpfDeTeste).orElseThrow();
        assertEquals("Nome alterado", existente.getNome(), "A exclusão deve usar o mesmo registro já alterado");
        abrirTela();
        WebElement linha = wait.until(ExpectedConditions.visibilityOfElementLocated(linhaDoCpf()));
        linha.findElement(By.cssSelector("button[aria-label^='Excluir']")).click();
        pausaEntreAcoes();
        Alert confirmacao = wait.until(ExpectedConditions.alertIsPresent());
        confirmacao.accept();
        pausaEntreAcoes();

        wait.until(d -> d.findElements(linhaDoCpf()).isEmpty());
        assertFalse(repository.existsByCpf(cpfDeTeste));
    }

    private void abrirTela() {
        driver.get("http://localhost:" + port + "/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-form")));
        wait.until(d -> !d.findElement(By.id("user-list")).getText().contains("Carregando usuários"));
        pausaEntreAcoes();
    }

    private void preencher(String nome, String cpf, LocalDate nascimento, String senha) {
        driver.findElement(By.id("nome")).sendKeys(nome);
        pausaEntreAcoes();
        driver.findElement(By.id("cpf")).sendKeys(cpf);
        pausaEntreAcoes();
        WebElement dataNascimento = driver.findElement(By.id("dataNascimento"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', {bubbles:true})); arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                dataNascimento, nascimento.toString());
        pausaEntreAcoes();
        driver.findElement(By.id("senha")).sendKeys(senha);
        pausaEntreAcoes();
    }

    private void pausaEntreAcoes() {
        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução do teste interrompida durante a pausa visual", e);
        }
    }

    private By linhaDoCpf() {
        return By.xpath("//tbody[@id='user-list']/tr[td[normalize-space()='%s']]".formatted(cpfDeTeste));
    }

}
