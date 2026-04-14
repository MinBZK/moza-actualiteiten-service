package nl.rijksoverheid.moz.actualiteiten.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import nl.rijksoverheid.moz.actualiteiten.dto.ArticlesResponse;
import nl.rijksoverheid.moz.actualiteiten.services.ArticlesService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/actualiteitenservice/v1/articles")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Articles", description = "Ondernemersplein articles, enriched with subjects and filtered in-memory")
public class ArticlesController {

    @Inject
    ArticlesService articlesService;

    @GET
    @Operation(
            summary = "Lijst artikelen",
            description = "Haalt een gefilterde lijst artikelen op. Service houdt een enriched cache met alle artikelen (inclusief subjects uit het detail-endpoint); filteren gebeurt in-memory."
    )
    public ArticlesResponse getArticles(
            @QueryParam("search") String search,
            @QueryParam("subject") List<String> subjects,
            @QueryParam("type") List<String> types,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit) {
        return articlesService.getArticles(search, subjects, types, offset, limit);
    }
}
