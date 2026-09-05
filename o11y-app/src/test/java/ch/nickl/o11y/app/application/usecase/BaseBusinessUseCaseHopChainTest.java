package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.client.DresdenClient;
import ch.nickl.o11y.app.infrastructure.client.FirenzeClient;
import ch.nickl.o11y.app.infrastructure.client.LondonClient;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wires the three real {@link BaseBusinessUseCase} subclasses together in-process, standing in
 * for the London/Firenze/Dresden containers, to prove that {@code callBusinessUseCase} performs a
 * genuine, decrementing, multi-service hop chain and that a service can land on itself as its own
 * next hop (a call is never excluded from picking its own client).
 */
class BaseBusinessUseCaseHopChainTest {

    private record Hop(String from, String to, int hopsAtCall, String delay) {
    }

    /** Wires {@code useCase} so each of its three next-hop clients records the hop and then really recurses into the target instance's own {@code callBusinessUseCase}. */
    private static void wire(BaseBusinessUseCase useCase, String fromName, List<Hop> log,
                              LondonBusinessUseCase london, FirenzeBusinessUseCase firenze, DresdenBusinessUseCase dresden) {
        useCase.londonClient = (LondonClient) (hops, delay) -> {
            log.add(new Hop(fromName, "London", hops, delay));
            return london.callBusinessUseCase(hops, delay);
        };
        useCase.firenzeClient = (FirenzeClient) (hops, delay) -> {
            log.add(new Hop(fromName, "Firenze", hops, delay));
            return firenze.callBusinessUseCase(hops, delay);
        };
        useCase.dresdenClient = (DresdenClient) (hops, delay) -> {
            log.add(new Hop(fromName, "Dresden", hops, delay));
            return dresden.callBusinessUseCase(hops, delay);
        };
    }

    private static String await(Uni<String> uni) {
        try {
            return uni.subscribeAsCompletionStage().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void given_hopsAtOrBelowOne_when_callingBusinessUseCase_then_noDownstreamHopIsMade() {
        List<Hop> log = new CopyOnWriteArrayList<>();
        DresdenBusinessUseCase dresden = new DresdenBusinessUseCase();
        LondonBusinessUseCase london = new LondonBusinessUseCase();
        FirenzeBusinessUseCase firenze = new FirenzeBusinessUseCase();
        wire(dresden, "Dresden", log, london, firenze, dresden);

        String response1 = await(dresden.callBusinessUseCase(1, "1ms"));
        String response0 = await(dresden.callBusinessUseCase(0, "1ms"));

        assertThat(log).isEmpty();
        assertThat(response1).isEqualTo("Dresden processed with hops: 1 and delay: 1ms");
        assertThat(response0).isEqualTo("Dresden processed with hops: 0 and delay: 1ms");
    }

    @Test
    void given_hopsAboveOne_when_callingBusinessUseCase_then_exactlyOneDownstreamHopIsMadeWithDecrementedHops() {
        List<Hop> log = new CopyOnWriteArrayList<>();
        DresdenBusinessUseCase dresden = new DresdenBusinessUseCase();
        // Terminal stubs (no recursion into a real use case) so the random pick can land on any
        // of the three without the chain continuing - keeping this assertion about Dresden's own
        // single hop, independent of what a next hop would itself go on to do.
        dresden.londonClient = (hops, delay) -> {
            log.add(new Hop("Dresden", "London", hops, delay));
            return Uni.createFrom().item("stub");
        };
        dresden.firenzeClient = (hops, delay) -> {
            log.add(new Hop("Dresden", "Firenze", hops, delay));
            return Uni.createFrom().item("stub");
        };
        dresden.dresdenClient = (hops, delay) -> {
            log.add(new Hop("Dresden", "Dresden", hops, delay));
            return Uni.createFrom().item("stub");
        };

        await(dresden.callBusinessUseCase(5, "1ms"));

        assertThat(log).hasSize(1);
        assertThat(log.get(0).from()).isEqualTo("Dresden");
        assertThat(log.get(0).hopsAtCall()).isEqualTo(4);
        assertThat(log.get(0).delay()).isEqualTo("1ms");
    }

    @Test
    void given_manyRequestsWithThreeHops_when_theChainRunsAcrossAllThreeWiredServices_thenItIsGenuinelyChainedAndCanSelfHop() {
        List<Hop> log = new CopyOnWriteArrayList<>();
        DresdenBusinessUseCase dresden = new DresdenBusinessUseCase();
        LondonBusinessUseCase london = new LondonBusinessUseCase();
        FirenzeBusinessUseCase firenze = new FirenzeBusinessUseCase();
        wire(dresden, "Dresden", log, london, firenze, dresden);
        wire(london, "London", log, london, firenze, dresden);
        wire(firenze, "Firenze", log, london, firenze, dresden);

        int runs = 60;
        for (int i = 0; i < runs; i++) {
            await(dresden.callBusinessUseCase(3, "1ms"));
        }

        // hops=3 always yields exactly two real hops before hitting the hops<=1 base case,
        // regardless of which service is randomly picked at each step.
        assertThat(log).hasSize(runs * 2);
        assertThat(log).filteredOn(hop -> hop.hopsAtCall() == 2).hasSize(runs);
        assertThat(log).filteredOn(hop -> hop.hopsAtCall() == 1).hasSize(runs);

        // Every hop is a real call into one of the three wired services...
        assertThat(log).extracting(Hop::to).containsOnly("London", "Firenze", "Dresden");
        // ...and, across enough trials, a service does land on itself as its own next hop -
        // self-calls are not excluded from the random candidate pool.
        assertThat(log).anyMatch(hop -> hop.from().equals(hop.to()));
        // The full three-way rotation is genuinely reachable, not just a subset.
        assertThat(log).extracting(Hop::to).contains("London", "Firenze", "Dresden");
    }
}
