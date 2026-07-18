package ch.nickl.o11y.dresden;

import ch.nickl.o11y.app.infrastructure.UseCase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import ch.nickl.o11y.app.application.usecase.DresdenBusinessUseCase;

@Path("/dresden")
public class DresdenResource {

    @Inject
    DresdenBusinessUseCase dresdenUseCase;

    @GET
    @UseCase
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callDresden(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        dresdenUseCase.callBusinessUseCase(hops, delay);
        return Uni.createFrom().item("Dresden processed with hops: " + hops + " and delay: " + delay);
    }
}
