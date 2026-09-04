package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.UseCase;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DresdenBusinessUseCase extends BaseBusinessUseCase {
    @Override
    void invoke() {
        Log.info("Dresden business logic output");
    }

    @Override
    protected String getUseCaseName() {
        return "Dresden";
    }
}
