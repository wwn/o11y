package ch.nickl.o11y.app.infrastructure.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/london")
@RegisterRestClient(configKey="london-api")
public interface LondonClient {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callLondon(@QueryParam("hops") int hops, @QueryParam("delay") String delay);
}
