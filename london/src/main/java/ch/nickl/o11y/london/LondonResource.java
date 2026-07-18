package ch.nickl.o11y.london;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ch.nickl.o11y.app.infrastructure.UseCase;
import jakarta.ws.rs.QueryParam;
import ch.nickl.o11y.app.application.usecase.LondenBusinessUseCase;

@Path("/london")
public class LondonResource {

    @Inject
    LondenBusinessUseCase londenUseCase;

    @GET
    @UseCase
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callLondon(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        londenUseCase.callBusinessUseCase(hops, delay);
        return Uni.createFrom().item("London processed with hops: " + hops + " and delay: " + delay);
    }
}
