package ch.nickl.o11y.firenze;

import ch.nickl.o11y.firenze.support.LondonDresdenWireMockResource;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is the automated version of manually curling {@code /dresden?hops=3} against the real
 * docker-compose stack and checking the logs: it proves that when firenze picks a random next hop
 * for a multi-hop request, the outbound call - a real REST-client HTTP request, made after the
 * Mutiny {@code delayIt().chain(...)} continuation - still carries the inbound correlation id and
 * the correctly decremented hop count. Firenze's own client for itself is pointed back at this
 * same running instance (see src/test/resources/application.properties), so a self-hop is
 * exercised over real HTTP too, not just simulated in-process.
 */
@QuarkusTest
@QuarkusTestResource(LondonDresdenWireMockResource.class)
class HopCorrelationPropagationTest {

    @Test
    void given_aMultiHopRequest_when_firenzePicksARandomNextHop_then_theOutboundCallCarriesTheSameCorrelationIdAndTheDecrementedHopCount() {
        WireMockServer london = LondonDresdenWireMockResource.london();
        WireMockServer dresden = LondonDresdenWireMockResource.dresden();

        // hops=3 always makes at least one real outbound hop before any base case is reached; the
        // random pick has only a 1-in-3 chance per hop of landing on firenze itself, so a handful
        // of attempts is enough to observe it landing on a mocked service with overwhelming
        // probability (failure probability after 20 tries is astronomically small), without
        // pinning down in advance which mocked service it will be.
        List<LoggedRequest> received = List.of();
        String correlationId = null;
        for (int attempt = 0; attempt < 20 && received.isEmpty(); attempt++) {
            correlationId = "hop-chain-" + UUID.randomUUID();
            given()
                    .header("X-Correlation-ID", correlationId)
            .when()
                    .get("/firenze?hops=3&delay=1ms")
            .then()
                    .statusCode(200);

            received = london.findAll(getRequestedFor(urlPathEqualTo("/london")));
            if (received.isEmpty()) {
                received = dresden.findAll(getRequestedFor(urlPathEqualTo("/dresden")));
            }
        }

        assertThat(received)
                .as("expected at least one of 20 hop-3 requests to reach the mocked london/dresden service")
                .isNotEmpty();

        LoggedRequest hopRequest = received.get(0);
        assertThat(hopRequest.getHeader("X-Correlation-ID")).isEqualTo(correlationId);
        assertThat(hopRequest.queryParameter("hops").firstValue()).isIn("1", "2");
        assertThat(hopRequest.queryParameter("delay").firstValue()).isEqualTo("1ms");
    }
}
