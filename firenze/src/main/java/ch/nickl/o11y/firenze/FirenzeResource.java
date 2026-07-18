package ch.nickl.o11y.firenze;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.QueryParam;
import ch.nickl.o11y.app.application.usecase.FirenzeBusinessUseCase;
import ch.nickl.o11y.app.infrastructure.UseCase;

@Path("/firenze")
public class FirenzeResource {

    @Inject
    FirenzeBusinessUseCase firenzeUseCase;

    @GET
    @UseCase
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callFirenze(@QueryParam("hops") int hops, @QueryParam("delay") String delay) {
        firenzeUseCase.callBusinessUseCase(hops, delay);
        return Uni.createFrom().item("Firenze processed with hops: " + hops + " and delay: " + delay);
    }
}
