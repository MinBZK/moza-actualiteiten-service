package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nl.rijksoverheid.moz.actualiteiten.services.IngestionStatusService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.Map;

@Path("/api/actualiteitenservice/v1/ingestion-status")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Ingestion", description = "Cache refresh status per source")
public class IngestionStatusController {

    @Inject
    IngestionStatusService ingestionStatusService;

    @GET
    @Operation(
            summary = "Status van bronnen",
            description = "Laatste succesvolle refresh per bron. Portaal gebruikt dit voor de 'laatst bijgewerkt' indicator."
    )
    public Map<String, Instant> status() {
        return ingestionStatusService.snapshot();
    }
}
