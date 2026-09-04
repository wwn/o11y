package ch.nickl.o11y.dresden;

import ch.nickl.o11y.app.application.usecase.DresdenBusinessUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dresden")
public class DresdenResource {

    @Inject
    DresdenBusinessUseCase dresdenUseCase;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callDresden(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        return dresdenUseCase.callBusinessUseCase(hops, delay);
    }
}
