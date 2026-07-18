package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.client.DresdenClient;
import ch.nickl.o11y.app.infrastructure.client.FirenzeClient;
import ch.nickl.o11y.app.infrastructure.client.LondonClient;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.MDC;

import java.util.Random;

import static ch.nickl.o11y.app.application.usecase.BusinessUseCase.parseDelayToMillis;

@Slf4j
public abstract class BaseBusinessUseCase implements BusinessUseCase {
    protected static final Random RANDOM = new Random();

    @ConfigProperty(name = "o11y.app.delay")
    String defaultDelay;

    @Inject
    Vertx vertx;

    @Inject
    @RestClient
    LondonClient londonClient;

    @Inject
    @RestClient
    FirenzeClient firenzeClient;

    @Inject
    @RestClient
    DresdenClient dresdenClient;

    protected abstract String getUseCaseName();

    @Override
    public void callBusinessUseCase(int hops, String delay) {
        String name = getUseCaseName();
        log.info("Executing {} logic with hops: {}, delay: {}", name, hops, delay);
        if (hops > 0) {
            int nextHops = hops - 1;
            long delayMs = parseDelayToMillis(delay);
            String correlationId = MDC.get("correlationId");
            vertx.setTimer(delayMs, id -> {
                if (correlationId != null) {
                    MDC.put("correlationId", correlationId);
                }
                int choice = RANDOM.nextInt(3);
                switch (choice) {
                    case 0 -> {
                        log.info("{} calling London with hops: {}, delay: {}", name, nextHops, delay);
                        londonClient.callLondon(nextHops, delay).subscribe().with(res -> log.info("London response: {}", res), err -> log.error("London call failed", err));
                    }
                    case 1 -> {
                        log.info("{} calling Firenze with hops: {}, delay: {}", name, nextHops, delay);
                        firenzeClient.callFirenze(nextHops, delay).subscribe().with(res -> log.info("Firenze response: {}", res), err -> log.error("Firenze call failed", err));
                    }
                    case 2 -> {
                        log.info("{} calling Dresden with hops: {}, delay: {}", name, nextHops, delay);
                        dresdenClient.callDresden(nextHops, delay).subscribe().with(res -> log.info("Dresden response: {}", res), err -> log.error("Dresden call failed", err));
                    }
                }
                MDC.clear();
            });
        }
    }
}
