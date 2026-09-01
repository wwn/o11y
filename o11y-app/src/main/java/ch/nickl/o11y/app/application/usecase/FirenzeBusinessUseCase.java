package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.UseCase;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

@ApplicationScoped
public class FirenzeBusinessUseCase extends BaseBusinessUseCase {
    @Override
    void invoke() {
        Log.info("Firenze business logic output");
    }

    @Override
    protected String getUseCaseName() {
        return "Firenze";
    }
}
