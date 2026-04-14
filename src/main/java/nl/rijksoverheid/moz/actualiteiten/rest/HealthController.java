package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/api/actualiteitenservice/v1/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Liveness probe")
public class HealthController {

    @GET
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}
