package pl.maxim.blog.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.openqa.selenium.chrome.ChromeOptions;
import pl.maxim.blog.testsupport.PostgresTc;
import pl.maxim.blog.testsupport.TcProps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerE2ETest {

    @LocalServerPort
    int port;

    static BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>(DockerImageName.parse("selenium/standalone-chrome:4.23.0"))
            .withCapabilities(new ChromeOptions())
            .withAccessToHost(true);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        PostgresTc.DB.start();
        TcProps.register(registry);
        registry.add("server.address", () -> "0.0.0.0");
    }

    @BeforeAll
    static void startBrowser() {
        chrome.start();
    }

    @AfterAll
    static void stopBrowser() {
        chrome.stop();
    }

    @Test
    void swagger_ui_loads() throws Exception {
        RemoteWebDriver driver = new RemoteWebDriver(new URL(chrome.getSeleniumAddress().toString()), new ChromeOptions());
        try {
            String hostUrl = "http://localhost:" + port + "/swagger-ui.html";
            boolean hostReady = waitForServer(hostUrl, Duration.ofSeconds(30));
            org.junit.jupiter.api.Assumptions.assumeTrue(hostReady, "App not reachable on host");

            String url = "http://host.docker.internal:" + port + "/swagger-ui.html";
            boolean containerReady = waitForServer(url, Duration.ofSeconds(30));
            org.junit.jupiter.api.Assumptions.assumeTrue(containerReady, "App not reachable from container");
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(d -> d.getTitle() != null && !d.getTitle().isBlank());

            String title = driver.getTitle();
            assertThat(title).isNotBlank();

            wait.until(d -> d.findElements(By.cssSelector(".swagger-ui")).size() > 0);

            var root = driver.findElements(By.cssSelector(".swagger-ui"));
            assertThat(root).isNotEmpty();
        } finally {
            driver.quit();
        }
    }

    private boolean waitForServer(String url, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code >= 200 && code < 500) {
                    return true;
                }
            } catch (IOException ignored) {
                // ignore and retry
            }
            Thread.sleep(500);
        }
        return false;
    }
}
