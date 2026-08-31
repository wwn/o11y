package ch.nickl.o11y.london;

import ch.nickl.o11y.app.application.usecase.LondonBusinessUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/london")
public class LondonResource {

    @Inject
    LondonBusinessUseCase londonUseCase;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callLondon(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        return londonUseCase.callBusinessUseCase(hops, delay);
    }
}
