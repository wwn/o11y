package ch.nickl.o11y.firenze;

import ch.nickl.o11y.app.application.usecase.FirenzeBusinessUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/firenze")
public class FirenzeResource {

    @Inject
    FirenzeBusinessUseCase firenzeUseCase;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callFirenze(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        return firenzeUseCase.callBusinessUseCase(hops, delay);
    }
}
