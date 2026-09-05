package ch.nickl.o11y.firenze.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Stands in for the london and dresden services during {@code firenze} tests, so a hop that picks
 * either of them lands on a real HTTP server whose received request a test can inspect - instead
 * of the request failing because no such service is actually running.
 */
public class LondonDresdenWireMockResource implements QuarkusTestResourceLifecycleManager {

    // Static: Quarkus owns the single instance of this manager, but tests need a handle on the
    // running servers too, so they're published here rather than kept as instance state.
    private static WireMockServer london;
    private static WireMockServer dresden;

    @Override
    public Map<String, String> start() {
        london = newStubServer("/london");
        dresden = newStubServer("/dresden");

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.rest-client.london-api.url", london.baseUrl());
        config.put("quarkus.rest-client.dresden-api.url", dresden.baseUrl());
        return config;
    }

    public static WireMockServer london() {
        return london;
    }

    public static WireMockServer dresden() {
        return dresden;
    }

    private static WireMockServer newStubServer(String path) {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        server.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("mocked")));
        return server;
    }

    @Override
    public void stop() {
        if (london != null) {
            london.stop();
        }
        if (dresden != null) {
            dresden.stop();
        }
    }
}
