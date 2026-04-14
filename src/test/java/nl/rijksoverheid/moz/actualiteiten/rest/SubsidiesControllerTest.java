package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.OndernemersPleinClient;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.PagingInfo;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieSummary;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidiesContract;
import nl.rijksoverheid.moz.actualiteiten.services.SubsidiesService;
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
import static org.mockito.Mockito.when;

@QuarkusTest
class SubsidiesControllerTest {

    @InjectMock
    @RestClient
    OndernemersPleinClient client;

    @Inject
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(SubsidiesService.CACHE_NAME)
                .ifPresent(c -> c.invalidateAll().await().indefinitely());

        SubsidieSummary s1 = summary("S1", "Innovatiekrediet");
        SubsidieSummary s2 = summary("S2", "Verduurzaming MKB");
        SubsidiesContract list = new SubsidiesContract();
        list.subsidies = List.of(s1, s2);
        PagingInfo p = new PagingInfo();
        p.total = 2;
        list.pagination = p;

        when(client.getSubsidies(any(), any(), anyInt(), anyInt())).thenReturn(list);
        when(client.getSubsidieDetail(eq("S1"))).thenReturn(
                detail("S1", List.of("Product, dienst en innovatie"), List.of("Nederland")));
        when(client.getSubsidieDetail(eq("S2"))).thenReturn(
                detail("S2", List.of("Klimaat, energie en natuur"), List.of("Zuid-Holland")));
    }

    @Test
    void getSubsidies_filtersByRegion() {
        given()
                .queryParam("region", "Zuid-Holland")
                .when().get("/api/actualiteitenservice/v1/subsidies")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("subsidies", hasSize(1))
                .body("subsidies[0].identifier", equalTo("S2"));
    }

    @Test
    void getSubsidies_filtersBySubject() {
        given()
                .queryParam("subject", "Product, dienst en innovatie")
                .when().get("/api/actualiteitenservice/v1/subsidies")
                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body("subsidies[0].identifier", equalTo("S1"));
    }

    private static SubsidieSummary summary(String id, String title) {
        SubsidieSummary s = new SubsidieSummary();
        s.identifier = id;
        s.title = title;
        return s;
    }

    private static SubsidieContract detail(String id, List<String> subjects, List<String> regions) {
        SubsidieContract c = new SubsidieContract();
        c.identifier = id;
        c.subjects = subjects;
        c.regions = regions;
        return c;
    }
}
