package ch.nickl.o11y.app.application.usecase;

import io.smallrye.mutiny.Uni;
import java.time.Duration;

public interface BusinessUseCase {
    Uni<String> callBusinessUseCase(int hops, String delay);

    static long parseDelayToMillis(String delayStr) {
        String isoDelay = delayStr.toUpperCase();
        if (!isoDelay.startsWith("PT") && !isoDelay.startsWith("P")) {
            isoDelay = "PT" + isoDelay;
        }
        long millis = Duration.parse(isoDelay).toMillis();
        return Math.max(millis, 1);
    }
}
