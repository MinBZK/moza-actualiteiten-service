package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import nl.rijksoverheid.moz.actualiteiten.dto.SubsidiesResponse;
import nl.rijksoverheid.moz.actualiteiten.services.SubsidiesService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/actualiteitenservice/v1/subsidies")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Subsidies", description = "Ondernemersplein subsidies, enriched and filtered in-memory")
public class SubsidiesController {

    @Inject
    SubsidiesService subsidiesService;

    @GET
    @Operation(
            summary = "Lijst subsidies",
            description = "Haalt een gefilterde lijst subsidies op. Service houdt een enriched cache met alle subsidies (inclusief subjects/regions uit het detail-endpoint); filteren gebeurt in-memory."
    )
    public SubsidiesResponse getSubsidies(
            @QueryParam("subject") List<String> subjects,
            @QueryParam("region") List<String> regions,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit) {
        return subsidiesService.getSubsidies(subjects, regions, offset, limit);
    }
}
