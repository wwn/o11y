package ch.nickl.o11y.app.application.usecase;

import java.time.Duration;

public interface BusinessUseCase {
    void callBusinessUseCase(int hops, String delay);

    static long parseDelayToMillis(String delayStr) {
        String isoDelay = delayStr.toUpperCase();
        if (!isoDelay.startsWith("PT") && !isoDelay.startsWith("P")) {
            isoDelay = "PT" + isoDelay;
        }
        return Duration.parse(isoDelay).toMillis();
    }
}
