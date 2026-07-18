package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.UseCase;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class FirenzeBusinessUseCase extends BaseBusinessUseCase {
    @Override
    void invoke() {
        log.info("Firenze business logic output");
    }

    @Override
    protected String getUseCaseName() {
        return "Firenze";
    }
}
