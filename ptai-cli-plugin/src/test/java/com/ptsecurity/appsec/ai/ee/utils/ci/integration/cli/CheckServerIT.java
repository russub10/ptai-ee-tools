package com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli.commands.BaseCommand;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.util.UUID;

@DisplayName("Server availability check tests")
@Tag("integration")
@Slf4j
class CheckServerIT extends BaseCliIT {
    @SneakyThrows
    @Test
    @DisplayName("Connect with valid token")
    public void testGoodToken(@NonNull final TestInfo testInfo) {
        log.trace(testInfo.getDisplayName());
        Integer res = new CommandLine(new Plugin()).execute(
                "check-server",
                "--url", CONNECTION().getUrl(),
                "--truststore", CA_PEM_FILE.toString(),
                "--token", CONNECTION().getToken());
        Assertions.assertEquals(BaseCommand.ExitCode.SUCCESS.getCode(), res);
    }

    @Test
    @DisplayName("Insecure connect with CA certificates")
    public void testInsecureWithCaCertificate(@NonNull final TestInfo testInfo) {
        log.trace(testInfo.getDisplayName());
        Integer res = new CommandLine(new Plugin()).execute(
                "check-server",
                "--url", CONNECTION().getUrl(),
                "--truststore", CA_PEM_FILE.toString(),
                "--token", CONNECTION().getToken(),
                "--insecure");
        Assertions.assertEquals(BaseCommand.ExitCode.SUCCESS.getCode(), res);
    }

    @Test
    @DisplayName("Insecure connect with valid token")
    public void testInsecureGoodToken(@NonNull final TestInfo testInfo) {
        log.trace(testInfo.getDisplayName());
        Integer res = new CommandLine(new Plugin()).execute(
                "check-server",
                "--url", CONNECTION().getUrl(),
                "--token", CONNECTION().getToken(),
                "--insecure");
        Assertions.assertEquals(BaseCommand.ExitCode.SUCCESS.getCode(), res);
    }

    @SneakyThrows
    @Test
    @DisplayName("Fail secure connect without valid CA certificates")
    public void testWithoutCaCertificates(@NonNull final TestInfo testInfo) {
        log.trace(testInfo.getDisplayName());
        Integer res = new CommandLine(new Plugin()).execute(
                "check-server",
                "--url", CONNECTION().getUrl(),
                "--token", CONNECTION().getToken(),
                "--truststore", DUMMY_CA_PEM_FILE.toString());
        Assertions.assertEquals(BaseCommand.ExitCode.FAILED.getCode(), res);
    }

    @Test
    @DisplayName("Fail connect with invalid token")
    public void testBadToken(@NonNull final TestInfo testInfo) {
        log.trace(testInfo.getDisplayName());
        Integer res = new CommandLine(new Plugin()).execute(
                "check-server",
                "--url", CONNECTION().getUrl(),
                "--truststore", CA_PEM_FILE.toString(),
                "--token", CONNECTION().getToken() + UUID.randomUUID());
        Assertions.assertEquals(BaseCommand.ExitCode.FAILED.getCode(), res);
    }
}