package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import nl.rijksoverheid.moz.actualiteiten.services.BerichtenService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/actualiteitenservice/v1/berichten")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Berichten", description = "Bekendmakingen uit de Overheid.nl SRU voor de postcode-voorkeuren van de partij")
public class BerichtenController {

    @Inject
    BerichtenService berichtenService;

    @GET
    @Path("/{identificatieType}/{identificatieNummer}")
    @Operation(
            summary = "Berichten voor een partij",
            description = "Leest de postcode-voorkeuren van de partij uit, haalt per postcode bekendmakingen op uit de SRU (gecached), en retourneert een gededupliceerde, op datum gesorteerde lijst."
    )
    public List<SruPublicatie> getBerichten(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer) {
        return berichtenService.getBerichtenForPartij(identificatieType, identificatieNummer);
    }
}
