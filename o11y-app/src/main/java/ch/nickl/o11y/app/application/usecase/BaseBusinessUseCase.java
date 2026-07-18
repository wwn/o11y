package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.client.DresdenClient;
import ch.nickl.o11y.app.infrastructure.client.FirenzeClient;
import ch.nickl.o11y.app.infrastructure.client.LondonClient;
import ch.nickl.o11y.app.infrastructure.UseCase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static ch.nickl.o11y.app.application.usecase.BusinessUseCase.parseDelayToMillis;

@Slf4j
public abstract class BaseBusinessUseCase implements BusinessUseCase {
    protected static final Random RANDOM = new Random();

    @ConfigProperty(name = "o11y.app.delay")
    String defaultDelay;

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

    abstract void invoke();

    @Override
    @UseCase
    public Uni<String> callBusinessUseCase(int hops, String delay) {
        invoke();
        String name = getUseCaseName();
        log.info("Executing {} post invoke calling with hops: {}, delay: {}", name, hops, delay);

        if (hops <= 1) {
            return Uni.createFrom().item(() -> buildResponse(name, hops, delay));
        }

        int nextHops = hops - 1;
        long delayMs = parseDelayToMillis(delay);

        List<NextUseCase> nextUseCases = List.of(
                new NextUseCase("London", () -> londonClient.callLondon(nextHops, delay)),
                new NextUseCase("Firenze", () -> firenzeClient.callFirenze(nextHops, delay)),
                new NextUseCase("Dresden", () -> dresdenClient.callDresden(nextHops, delay))
        );

        NextUseCase nextUseCase = nextUseCases.get(RANDOM.nextInt(nextUseCases.size()));

        return Uni.createFrom().item(0)
                .onItem().delayIt().by(Duration.ofMillis(delayMs))
                .chain(() -> {
                    log.info("{} calling {} with hops: {}, delay: {}", name, nextUseCase.name(), nextHops, delay);
                    return nextUseCase.call().get()
                            .onFailure().retry().atMost(3)
                            .invoke(res -> log.info("{} response: {}", nextUseCase.name(), res))
                            .replaceWith(buildResponse(name, hops, delay));
                });
    }

    private String buildResponse(String name, int hops, String delay) {
        return name + " processed with hops: " + hops + " and delay: " + delay;
    }

    private record NextUseCase(String name, Supplier<Uni<String>> call) {
    }
}
