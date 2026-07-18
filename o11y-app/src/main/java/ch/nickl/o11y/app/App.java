package ch.nickl.o11y.app;

import ch.nickl.o11y.app.application.usecase.BusinessUseCase;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ApplicationScoped
public class App {
    private static final Random RANDOM = new Random();

    @ConfigProperty(name = "o11y.app.hops")
    int initialHops;

    @ConfigProperty(name = "o11y.app.delay")
    String delay;

    @Inject
    Vertx vertx;

    @Inject
    Instance<BusinessUseCase> useCases;

    private final AtomicInteger currentHops = new AtomicInteger(-1);

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting. Waiting 10 seconds for other services to be ready...");
        currentHops.set(initialHops);
        vertx.setTimer(10_000L, id -> {
            log.info("Starting initial use case call sequence (Hops: {}, Delay: {}).", initialHops, delay);
            callRandomUseCase();
        });
    }

    private void callRandomUseCase() {
        List<BusinessUseCase> list = new ArrayList<>();
        useCases.forEach(list::add);

        if (!list.isEmpty()) {
            BusinessUseCase selected = list.get(RANDOM.nextInt(list.size()));
            int hopsToPass = currentHops.getAndDecrement();
            
            if (hopsToPass >= 0) {
                log.info("Triggering sequence: {} with {} hops remaining.", selected.getClass().getSimpleName(), hopsToPass);
                selected.callBusinessUseCase(hopsToPass, delay);
            } else {
                log.info("Initial hops exhausted. No more automatic calls.");
            }
        } else {
            log.warn("No BusinessUseCase implementations found!");
        }
    }
}
