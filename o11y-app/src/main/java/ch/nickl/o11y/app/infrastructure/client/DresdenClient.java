package ch.nickl.o11y.app.infrastructure.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import ch.nickl.o11y.app.infrastructure.logging.CorrelationIdClientFilter;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/dresden")
@RegisterRestClient(configKey="dresden-api")
@RegisterProvider(CorrelationIdClientFilter.class)
public interface DresdenClient {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callDresden(@QueryParam("hops") int hops, @QueryParam("delay") String delay);
}
