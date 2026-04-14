package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleSummary;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticlesContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.OndernemersPleinClient;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.PagingInfo;
import nl.rijksoverheid.moz.actualiteiten.services.ArticlesService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ArticlesControllerTest {

    @InjectMock
    @RestClient
    OndernemersPleinClient client;

    @Inject
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(ArticlesService.CACHE_NAME)
                .ifPresent(c -> c.invalidateAll().await().indefinitely());

        ArticleSummary a1 = summary("A1", "Belasting nieuws", "artikel-nl");
        ArticleSummary a2 = summary("A2", "Bouwregels 2026", "regel-nl");
        ArticleSummary a3 = summary("A3", "Horeca tips", "artikel-nl");
        ArticlesContract list = new ArticlesContract();
        list.articles = List.of(a1, a2, a3);
        PagingInfo p = new PagingInfo();
        p.total = 3;
        list.pagination = p;

        when(client.getArticles(any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(list);
        when(client.getArticleDetail(eq("A1"))).thenReturn(detail("A1", List.of("Belastingen en heffingen")));
        when(client.getArticleDetail(eq("A2"))).thenReturn(detail("A2", List.of("Omgevingswet")));
        when(client.getArticleDetail(eq("A3"))).thenReturn(detail("A3", List.of("Arbeidsvoorwaarden")));
    }

    @Test
    void getArticles_returnsAllWhenUnfiltered() {
        given()
                .when().get("/api/actualiteitenservice/v1/articles")
                .then()
                .statusCode(200)
                .body("total", equalTo(3))
                .body("articles", hasSize(3));
    }

    @Test
    void getArticles_filtersBySubject() {
        given()
                .queryParam("subject", "Omgevingswet")
                .when().get("/api/actualiteitenservice/v1/articles")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("articles[0].identifier", equalTo("A2"))
                .body("articles[0].subjects[0]", equalTo("Omgevingswet"));
    }

    @Test
    void getArticles_filtersByType() {
        given()
                .queryParam("type", "regel-nl")
                .when().get("/api/actualiteitenservice/v1/articles")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("articles[0].identifier", equalTo("A2"));
    }

    @Test
    void getArticles_searchMatchesHeadline() {
        given()
                .queryParam("search", "horeca")
                .when().get("/api/actualiteitenservice/v1/articles")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("articles[0].identifier", equalTo("A3"));
    }

    @Test
    void getArticles_paginates() {
        given()
                .queryParam("limit", 2)
                .queryParam("offset", 1)
                .when().get("/api/actualiteitenservice/v1/articles")
                .then()
                .statusCode(200)
                .body("total", equalTo(3))
                .body("articles", hasSize(2));
    }

    @Test
    void enrichedCache_isLoadedOnceAcrossQueries() {
        given().when().get("/api/actualiteitenservice/v1/articles").then().statusCode(200);
        given().queryParam("subject", "Omgevingswet").when().get("/api/actualiteitenservice/v1/articles").then().statusCode(200);
        given().queryParam("type", "artikel-nl").when().get("/api/actualiteitenservice/v1/articles").then().statusCode(200);

        verify(client, times(1)).getArticles(any(), any(), any(), anyInt(), anyInt(), any());
        verify(client, times(1)).getArticleDetail("A1");
        verify(client, times(1)).getArticleDetail("A2");
        verify(client, times(1)).getArticleDetail("A3");
    }

    private static ArticleSummary summary(String id, String head, String type) {
        ArticleSummary s = new ArticleSummary();
        s.identifier = id;
        s.headLine = head;
        s.additionalType = type;
        return s;
    }

    private static ArticleContract detail(String id, List<String> subjects) {
        ArticleContract c = new ArticleContract();
        c.identifier = id;
        c.subjects = subjects;
        return c;
    }
}
