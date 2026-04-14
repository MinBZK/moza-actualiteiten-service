package nl.rijksoverheid.moz.actualiteiten.external.profielservice;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the Profiel Service koppelcode endpoint.
 * <p>
 * Nevenservices use the koppelcode as a stable, non-guessable identifier for a
 * partij, so they can store data without ever learning the underlying BSN or
 * KVK (zie ADR 0016).
 */
@RegisterRestClient(configKey = "profiel-service")
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/profielservice/v1/koppelcode")
public interface ProfielServiceClient {

    @GET
    @Path("/{identificatieType}/{identificatieNummer}")
    KoppelcodeResponse getKoppelcode(
            @PathParam("identificatieType") String identificatieType,
            @PathParam("identificatieNummer") String identificatieNummer);
}
