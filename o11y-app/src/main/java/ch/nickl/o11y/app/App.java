package ch.nickl.o11y.app;

import ch.nickl.o11y.app.infrastructure.client.DresdenClient;
import ch.nickl.o11y.app.infrastructure.client.FirenzeClient;
import ch.nickl.o11y.app.infrastructure.client.LondonClient;
import ch.nickl.o11y.app.infrastructure.logging.CorrelationId;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logmanager.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

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
    @RestClient
    LondonClient londonClient;

    @Inject
    @RestClient
    FirenzeClient firenzeClient;

    @Inject
    @RestClient
    DresdenClient dresdenClient;

    @ConfigProperty(name = "o11y.app.initial-sequence.enabled", defaultValue = "false")
    boolean initialSequenceEnabled;

    void onStart(@Observes StartupEvent ev) {
        if (initialSequenceEnabled) {
            Log.infof("Starting initial call sequence (Hops: %s, Delay: %s) in 10s.", initialHops, delay);
            vertx.setTimer(10000, id -> callRandomUseCase());
        }
    }

    private void callRandomUseCase() {
        Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
        context.runOnContext(v -> {
            MDC.put(CorrelationId.MDC_KEY, UUID.randomUUID().toString());

            List<NextCall> calls = List.of(
                    new NextCall("London", () -> londonClient.callLondon(initialHops, delay)),
                    new NextCall("Firenze", () -> firenzeClient.callFirenze(initialHops, delay)),
                    new NextCall("Dresden", () -> dresdenClient.callDresden(initialHops, delay))
            );

            NextCall selected = calls.get(RANDOM.nextInt(calls.size()));

            Log.infof("Triggering initial REST call to: %s with %s initial hops.", selected.name(), initialHops);
            selected.call().get().subscribe().with(
                    res -> Log.infof("Initial sequence completed: %s", res),
                    err -> Log.error("Initial sequence failed", err)
            );
        });
    }

    private record NextCall(String name, Supplier<Uni<String>> call) {}
}
