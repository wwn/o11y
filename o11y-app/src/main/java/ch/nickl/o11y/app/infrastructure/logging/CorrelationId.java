package ch.nickl.o11y.app.infrastructure.logging;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-ID";

    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }
}
