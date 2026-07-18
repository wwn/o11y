package ch.nickl.o11y.app.application.usecase;

import ch.nickl.o11y.app.infrastructure.UseCase;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@UseCase
public class LondonBusinessUseCase extends BaseBusinessUseCase {
    @Override
    protected String getUseCaseName() {
        return "London";
    }
}
