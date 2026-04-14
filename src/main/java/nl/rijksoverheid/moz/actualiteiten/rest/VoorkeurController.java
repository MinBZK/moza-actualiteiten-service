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
import nl.rijksoverheid.moz.actualiteiten.services.VoorkeurService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/actualiteitenservice/v1/voorkeuren")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Voorkeuren", description = "Postcode-, onderwerp- en favoriet-voorkeuren per partij (via koppelcode)")
public class VoorkeurController {

    @Inject
    VoorkeurService voorkeurService;

    @GET
    @Path("/{identificatieType}/{identificatieNummer}")
    @Operation(summary = "Alle voorkeuren voor een partij")
    public VoorkeurenResponse getAll(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer) {
        return voorkeurService.getAll(identificatieType, identificatieNummer);
    }

    @POST
    @Path("/postcode/{identificatieType}/{identificatieNummer}")
    @Operation(summary = "Voeg postcode-voorkeur toe")
    public Response addPostcode(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            PostcodeRequest request) {
        voorkeurService.addPostcode(identificatieType, identificatieNummer, request.postcode);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/postcode/{identificatieType}/{identificatieNummer}/{id}")
    @Operation(summary = "Verwijder postcode-voorkeur")
    public Response deletePostcode(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            @PathParam("id") Long id) {
        boolean deleted = voorkeurService.deletePostcode(identificatieType, identificatieNummer, id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/onderwerp/{identificatieType}/{identificatieNummer}")
    @Operation(summary = "Voeg onderwerp-voorkeur toe")
    public Response addOnderwerp(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            OnderwerpRequest request) {
        voorkeurService.addOnderwerp(identificatieType, identificatieNummer, request.onderwerp);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/onderwerp/{identificatieType}/{identificatieNummer}/{id}")
    @Operation(summary = "Verwijder onderwerp-voorkeur")
    public Response deleteOnderwerp(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            @PathParam("id") Long id) {
        boolean deleted = voorkeurService.deleteOnderwerp(identificatieType, identificatieNummer, id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/favoriet/{identificatieType}/{identificatieNummer}")
    @Operation(summary = "Voeg favoriet artikel toe")
    public Response addFavoriet(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            FavorietRequest request) {
        voorkeurService.addFavoriet(identificatieType, identificatieNummer, request.articleId, request.articleType);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/favoriet/{identificatieType}/{identificatieNummer}/{id}")
    @Operation(summary = "Verwijder favoriet artikel")
    public Response deleteFavoriet(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer,
            @PathParam("id") Long id) {
        boolean deleted = voorkeurService.deleteFavoriet(identificatieType, identificatieNummer, id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}
