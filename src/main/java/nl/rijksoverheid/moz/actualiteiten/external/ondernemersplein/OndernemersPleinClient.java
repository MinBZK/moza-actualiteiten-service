package nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * REST client for the Ondernemersplein OpenData API.
 * <p>
 * Public endpoint, no auth required. The list endpoints return summary records
 * (no {@code subjects}); the detail endpoints return {@code ArticleContract}
 * and {@code SubsidieContract} which include subjects.
 */
@RegisterRestClient(configKey = "ondernemersplein")
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v1")
public interface OndernemersPleinClient {

    @GET
    @Path("/articles")
    ArticlesContract getArticles(
            @QueryParam("search") String search,
            @QueryParam("subject") List<String> subjects,
            @QueryParam("type") List<String> types,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @QueryParam("order") String order);

    @GET
    @Path("/articles/{id}")
    ArticleContract getArticleDetail(@PathParam("id") String id);

    @GET
    @Path("/subsidies")
    SubsidiesContract getSubsidies(
            @QueryParam("subjects") List<String> subjects,
            @QueryParam("regions") List<String> regions,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit);

    @GET
    @Path("/subsidies/{id}")
    SubsidieContract getSubsidieDetail(@PathParam("id") String id);
}
