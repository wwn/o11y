package ch.nickl.o11y.app.infrastructure;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logmanager.MDC;

@UseCase
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class UseCaseInterceptor {

    @AroundInvoke
    public Object logUseCaseExecution(InvocationContext context) throws Exception {
        String rawClassName = context.getTarget().getClass().getSimpleName();
        String useCase = rawClassName.replaceAll("\\$.*", "").replace("_Subclass", "");
        String method = context.getMethod().getName();

        MDC.put("useCase", useCase);
        MDC.put("method", method);
        long startNanos = System.nanoTime();
        Log.infof("UseCase started: %s", useCase);

        try {
            Object result = context.proceed();

            if (result instanceof Uni<?> uni) {
                return uni
                        .onItemOrFailure().invoke((item, failure) -> logOutcome(useCase, startNanos, failure))
                        .onTermination().invoke(UseCaseInterceptor::clearScopeKeys);
            }

            logOutcome(useCase, startNanos, null);
            clearScopeKeys();
            return result;
        } catch (Exception e) {
            logOutcome(useCase, startNanos, e);
            clearScopeKeys();
            throw e;
        }
    }

    private void logOutcome(String useCase, long startNanos, Throwable failure) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        MDC.put("durationMs", Long.toString(durationMs));
        try {
            if (failure == null) {
                MDC.put("outcome", "SUCCESS");
                Log.infof("UseCase finished: %s (%dms)", useCase, durationMs);
            } else {
                MDC.put("outcome", "FAILURE");
                MDC.put("errorReason", failure.toString());
                Log.errorf(failure, "UseCase failed: %s (%dms)", useCase, durationMs);
            }
        } finally {
            MDC.remove("durationMs");
            MDC.remove("outcome");
            MDC.remove("errorReason");
        }
    }

    private static void clearScopeKeys() {
        MDC.remove("useCase");
        MDC.remove("method");
    }
}
