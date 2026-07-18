package ch.nickl.o11y.app.infrastructure;

import io.smallrye.mutiny.Uni;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import java.util.UUID;

@Slf4j
@UseCase
@Interceptor
public class UseCaseInterceptor {
    @AroundInvoke
    public Object logUseCaseExecution(InvocationContext context) throws Exception {
        String rawClassName = context.getTarget().getClass().getSimpleName();
        String className = rawClassName.replaceAll("\\$.*", "").replace("_Subclass", "");
        String methodName = context.getMethod().getName();


        if (MDC.get("correlationId") == null) {
            MDC.put("correlationId", UUID.randomUUID().toString());
        }

        MDC.put("useCase", className);
        MDC.put("method", methodName);
        MDC.put("durationMs", "0");
        MDC.put("status", "STARTED");

        Object[] parameters = context.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null) {
                    MDC.put("param" + i, parameters[i].toString());
                }
            }
        }

        long startTime = System.currentTimeMillis();
        log.info("UseCase started: {}", className);

        try {
            Object result = context.proceed();
            if (result instanceof Uni<?> uni) {
                var contextMap = MDC.getCopyOfContextMap();
                return uni
                        .onItem().invoke(item -> {
                            if (contextMap != null) MDC.setContextMap(contextMap);
                            finalizeMdc(className, startTime, "SUCCESS", null);
                        })
                        .onFailure().invoke(throwable -> {
                            if (contextMap != null) MDC.setContextMap(contextMap);
                            finalizeMdc(className, startTime, "FAILURE", throwable.toString());
                        })
                        .onTermination().invoke(MDC::clear);
            }

            finalizeMdc(className, startTime, "SUCCESS", null);
            return result;
        } catch (Exception e) {
            finalizeMdc(className, startTime, "FAILURE", e.getMessage());
            throw e;
        } finally {
            clearMdc(parameters);
        }
    }

    private void finalizeMdc(String className, long startTime, String status, String errorReason) {
        long duration = System.currentTimeMillis() - startTime;
        MDC.put("durationMs", String.valueOf(duration));
        MDC.put("status", status);
        if (errorReason != null) {
            MDC.put("errorReason", errorReason);
        }
        if ("FAILURE".equals(status)) {
            log.error("UseCase failed: {} - {}", className, errorReason);
        } else {
            log.info("UseCase finished: {} (duration: {}ms)", className, duration);
        }
    }

    private void clearMdc(Object[] parameters) {
        MDC.remove("useCase");
        MDC.remove("method");
        MDC.remove("durationMs");
        MDC.remove("status");
        MDC.remove("errorReason");
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                MDC.remove("param" + i);
            }
        }
    }
}
