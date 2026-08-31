package ch.nickl.o11y.app.infrastructure.logging;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logmanager.MDC;

import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class CorrelationIdServerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String correlationId = requestContext.getHeaderString(CorrelationId.HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CorrelationId.MDC_KEY, correlationId);
        requestContext.setProperty(CorrelationId.MDC_KEY, correlationId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object correlationId = requestContext.getProperty(CorrelationId.MDC_KEY);
        if (correlationId != null) {
            responseContext.getHeaders().putSingle(CorrelationId.HEADER, correlationId);
        }
        MDC.remove(CorrelationId.MDC_KEY);
    }
}
