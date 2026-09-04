package ch.nickl.o11y.app.infrastructure.logging;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logmanager.MDC;

import java.util.UUID;

@Provider
public class CorrelationIdClientFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) {
        String correlationId = MDC.get(CorrelationId.MDC_KEY);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        requestContext.getHeaders().putSingle(CorrelationId.HEADER, correlationId);
    }
}
