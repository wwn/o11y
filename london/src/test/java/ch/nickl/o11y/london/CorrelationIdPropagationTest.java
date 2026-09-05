package ch.nickl.o11y.london;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

@QuarkusTest
class CorrelationIdPropagationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private RecordingHandler handler;
    private Logger rootLogger;

    @BeforeEach
    void attachHandler() {
        handler = new RecordingHandler();
        handler.setLevel(Level.ALL);
        rootLogger = LogContext.getLogContext().getLogger("");
        rootLogger.addHandler(handler);
    }

    @AfterEach
    void detachHandler() {
        rootLogger.removeHandler(handler);
    }

    @Test
    void given_anInboundCorrelationIdHeader_when_callingLondon_then_itIsEchoedOnTheResponse() {
        given()
                .header("X-Correlation-ID", "trace-abc-123")
        .when()
                .get("/london?hops=1&delay=1ms")
        .then()
                .statusCode(200)
                .header("X-Correlation-ID", "trace-abc-123");
    }

    @Test
    void given_noCorrelationIdHeader_when_callingLondon_then_aValidUuidIsMintedAndReturned() {
        String generated = given()
        .when()
                .get("/london?hops=1&delay=1ms")
        .then()
                .statusCode(200)
                .extract().header("X-Correlation-ID");

        assertThat(generated).isNotBlank();
        assertThatCode(() -> UUID.fromString(generated)).doesNotThrowAnyException();
    }

    @Test
    void given_anInboundCorrelationId_when_theUseCaseCompletesReactively_then_theCompletionLogRecordStillCarriesTheMdc() {
        given()
                .header("X-Correlation-ID", "trace-success-1")
        .when()
                .get("/london?hops=1&delay=1ms")
        .then()
                .statusCode(200);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            ExtLogRecord finished = handler.lastRecordStartingWith("UseCase finished")
                    .orElseThrow(() -> new AssertionError("no 'UseCase finished' record yet; saw " + handler.messages()));

            assertThat(finished.getMdc("correlationId")).isEqualTo("trace-success-1");
            assertThat(finished.getMdc("useCase")).isEqualTo("LondonBusinessUseCase");
            assertThat(finished.getMdc("outcome")).isEqualTo("SUCCESS");
            assertThat(finished.getMdc("durationMs")).isNotBlank();
        });
    }

    @Test
    void given_anInboundCorrelationId_when_theUseCaseStarts_then_perEventMdcFieldsAreNotYetPresent() {
        given()
                .header("X-Correlation-ID", "trace-scope-1")
        .when()
                .get("/london?hops=1&delay=1ms")
        .then()
                .statusCode(200);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            ExtLogRecord started = handler.lastRecordStartingWith("UseCase started")
                    .orElseThrow(() -> new AssertionError("no 'UseCase started' record yet; saw " + handler.messages()));

            assertThat(started.getMdc("correlationId")).isEqualTo("trace-scope-1");
            assertThat(started.getMdc("useCase")).isEqualTo("LondonBusinessUseCase");
            // durationMs / outcome / errorReason belong only on the completion record
            assertThat(started.getMdc("outcome")).isNull();
            assertThat(started.getMdc("durationMs")).isNull();
            assertThat(started.getMdc("errorReason")).isNull();
        });
    }

    @Test
    void given_anUnparseableDelay_when_callingLondon_then_itReturns400AndLogsACorrelatedFailure() {
        given()
                .header("X-Correlation-ID", "trace-fail-9")
        .when()
                .get("/london?hops=2&delay=nonsense")
        .then()
                .statusCode(400);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            ExtLogRecord failed = handler.lastRecordStartingWith("UseCase failed")
                    .orElseThrow(() -> new AssertionError("no 'UseCase failed' record yet; saw " + handler.messages()));

            assertThat(failed.getMdc("correlationId")).isEqualTo("trace-fail-9");
            assertThat(failed.getMdc("outcome")).isEqualTo("FAILURE");
            assertThat(failed.getMdc("errorReason")).contains("delay");
        });
    }
}
