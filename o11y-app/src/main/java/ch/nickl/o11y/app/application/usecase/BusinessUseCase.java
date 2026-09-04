package ch.nickl.o11y.app.application.usecase;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface BusinessUseCase {

    Uni<String> callBusinessUseCase(int hops, String delay);

    static long parseDelayToMillis(String delayStr) {
        if (delayStr == null || delayStr.isBlank()) {
            throw new BadRequestException("delay must not be empty; use e.g. '250ms', '2s', '1.5m' or ISO-8601 'PT2S'");
        }
        String value = delayStr.trim();

        Matcher shorthand = Pattern.compile("(?i)^(\\d+(?:\\.\\d+)?)\\s*(ms|s|m|h)?$").matcher(value);
        if (shorthand.matches()) {
            double amount = Double.parseDouble(shorthand.group(1));
            String unit = shorthand.group(2) == null ? "ms" : shorthand.group(2).toLowerCase();
            double millis = switch (unit) {
                case "ms" -> amount;
                case "s" -> amount * 1_000;
                case "m" -> amount * 60_000;
                case "h" -> amount * 3_600_000;
                default -> throw new IllegalStateException("unreachable delay unit: " + unit);
            };
            return Math.max((long) millis, 1);
        }

        try {
            String iso = value.toUpperCase();
            if (!iso.startsWith("P")) {
                iso = "PT" + iso;
            }
            return Math.max(Duration.parse(iso).toMillis(), 1);
        } catch (DateTimeParseException | ArithmeticException e) {
            throw new BadRequestException(
                    "Unsupported delay '" + delayStr + "'; use e.g. '250ms', '2s', '1.5m' or ISO-8601 'PT2S'");
        }
    }
}
