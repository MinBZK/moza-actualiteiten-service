package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.rijksoverheid.moz.actualiteiten.dto.FavorietRequest;
import nl.rijksoverheid.moz.actualiteiten.dto.OnderwerpRequest;
import nl.rijksoverheid.moz.actualiteiten.dto.PostcodeRequest;
import nl.rijksoverheid.moz.actualiteiten.dto.VoorkeurenResponse;
import nl.rijksoverheid.moz.actualiteiten.security.SubjectIdContext;
import nl.rijksoverheid.moz.actualiteiten.services.VoorkeurService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/actualiteitenservice/v1/voorkeuren")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Voorkeuren", description = "Postcode-, onderwerp- en favoriet-voorkeuren per partij")
public class VoorkeurController {

    @Inject
    VoorkeurService voorkeurService;

    @Inject
    SubjectIdContext subjectIdContext;

    @GET
    @Operation(summary = "Alle voorkeuren voor de ingelogde partij")
    public VoorkeurenResponse getAll() {
        return voorkeurService.getAll(subjectIdContext.requireSubjectId());
    }

    @POST
    @Path("/postcode")
    @Operation(summary = "Voeg postcode-voorkeur toe")
    public Response addPostcode(PostcodeRequest request) {
        voorkeurService.addPostcode(subjectIdContext.requireSubjectId(), request.postcode);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/postcode/{id}")
    @Operation(summary = "Verwijder postcode-voorkeur")
    public Response deletePostcode(@PathParam("id") Long id) {
        boolean deleted = voorkeurService.deletePostcode(subjectIdContext.requireSubjectId(), id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/onderwerp")
    @Operation(summary = "Voeg onderwerp-voorkeur toe")
    public Response addOnderwerp(OnderwerpRequest request) {
        voorkeurService.addOnderwerp(subjectIdContext.requireSubjectId(), request.onderwerp);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/onderwerp/{id}")
    @Operation(summary = "Verwijder onderwerp-voorkeur")
    public Response deleteOnderwerp(@PathParam("id") Long id) {
        boolean deleted = voorkeurService.deleteOnderwerp(subjectIdContext.requireSubjectId(), id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/favoriet")
    @Operation(summary = "Voeg favoriet artikel toe")
    public Response addFavoriet(FavorietRequest request) {
        voorkeurService.addFavoriet(subjectIdContext.requireSubjectId(), request.articleId, request.articleType);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/favoriet/{id}")
    @Operation(summary = "Verwijder favoriet artikel")
    public Response deleteFavoriet(@PathParam("id") Long id) {
        boolean deleted = voorkeurService.deleteFavoriet(subjectIdContext.requireSubjectId(), id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}
