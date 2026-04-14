package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.KoppelcodeResponse;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.ProfielServiceClient;
import nl.rijksoverheid.moz.actualiteiten.external.sru.SruClient;
import nl.rijksoverheid.moz.actualiteiten.services.KoppelcodeResolverService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BerichtenControllerTest {

    private static final UUID KOPPELCODE = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @InjectMock
    @RestClient
    ProfielServiceClient profielService;

    @InjectMock
    SruClient sruClient;

    @Inject
    CacheManager cacheManager;

    @BeforeEach
    @Transactional
    void setUp() {
        PostcodeVoorkeur.deleteAll();
        cacheManager.getCache(KoppelcodeResolverService.CACHE_NAME)
                .ifPresent(c -> c.invalidateAll().await().indefinitely());

        KoppelcodeResponse kc = new KoppelcodeResponse();
        kc.koppelcode = KOPPELCODE;
        when(profielService.getKoppelcode(anyString(), anyString())).thenReturn(kc);
    }

    @Test
    void berichten_emptyWhenNoPostcodes() {
        given()
                .when().get("/api/actualiteitenservice/v1/berichten/BSN/123456789")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void berichten_combinesAndDeduplicatesAcrossPostcodes() {
        given().contentType(ContentType.JSON).body("{\"postcode\":\"1000AA\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/123")
                .then().statusCode(201);
        given().contentType(ContentType.JSON).body("{\"postcode\":\"2000BB\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/123")
                .then().statusCode(201);

        SruPublicatie p1 = pub("P1", "2026-04-10");
        SruPublicatie p2 = pub("P2", "2026-04-12");
        SruPublicatie p1Duplicate = pub("P1", "2026-04-10");

        when(sruClient.fetchByPostcode("1000AA")).thenReturn(List.of(p1));
        when(sruClient.fetchByPostcode("2000BB")).thenReturn(List.of(p2, p1Duplicate));

        given()
                .when().get("/api/actualiteitenservice/v1/berichten/BSN/123")
                .then().statusCode(200)
                .body("$", hasSize(2))
                // Sorted by modified desc
                .body("[0].id", equalTo("P2"))
                .body("[1].id", equalTo("P1"));

        verify(sruClient).fetchByPostcode("1000AA");
        verify(sruClient).fetchByPostcode("2000BB");
    }

    private static SruPublicatie pub(String id, String modified) {
        SruPublicatie p = new SruPublicatie();
        p.id = id;
        p.title = "Bekendmaking " + id;
        p.modified = modified;
        return p;
    }
}
