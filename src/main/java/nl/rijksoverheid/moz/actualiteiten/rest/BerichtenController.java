package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import nl.rijksoverheid.moz.actualiteiten.security.SubjectIdContext;
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

    @Inject
    SubjectIdContext subjectIdContext;

    @GET
    @Operation(
            summary = "Berichten voor de ingelogde partij",
            description = "Leest de postcode-voorkeuren van de partij uit, haalt per postcode bekendmakingen op uit de SRU (gecached), en retourneert een gededupliceerde, op datum gesorteerde lijst."
    )
    public List<SruPublicatie> getBerichten() {
        return berichtenService.getBerichtenForPartij(subjectIdContext.requireSubjectId());
    }
}
