package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.entity.FavorietArtikel;
import nl.rijksoverheid.moz.actualiteiten.entity.OnderwerpVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.KoppelcodeResponse;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.ProfielServiceClient;
import nl.rijksoverheid.moz.actualiteiten.services.KoppelcodeResolverService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class VoorkeurControllerTest {

    private static final UUID KOPPELCODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @InjectMock
    @RestClient
    ProfielServiceClient profielService;

    @Inject
    CacheManager cacheManager;

    @BeforeEach
    @Transactional
    void setUp() {
        PostcodeVoorkeur.deleteAll();
        OnderwerpVoorkeur.deleteAll();
        FavorietArtikel.deleteAll();
        cacheManager.getCache(KoppelcodeResolverService.CACHE_NAME)
                .ifPresent(c -> c.invalidateAll().await().indefinitely());

        KoppelcodeResponse kc = new KoppelcodeResponse();
        kc.koppelcode = KOPPELCODE;
        when(profielService.getKoppelcode(anyString(), anyString())).thenReturn(kc);
    }

    @Test
    void postcodeCrud_roundTrip() {
        given().contentType(ContentType.JSON).body("{\"postcode\":\"1012AB\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/123456789")
                .then().statusCode(201);

        given()
                .when().get("/api/actualiteitenservice/v1/voorkeuren/BSN/123456789")
                .then().statusCode(200)
                .body("postcodes", hasSize(1))
                .body("postcodes[0].postcode", equalTo("1012AB"));

        Long id = given().when().get("/api/actualiteitenservice/v1/voorkeuren/BSN/123456789")
                .then().extract().jsonPath().getLong("postcodes[0].id");

        given()
                .when().delete("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/123456789/" + id)
                .then().statusCode(204);

        given()
                .when().get("/api/actualiteitenservice/v1/voorkeuren/BSN/123456789")
                .then().statusCode(200)
                .body("postcodes", hasSize(0));
    }

    @Test
    void onderwerp_andFavoriet_alsoRoundTrip() {
        given().contentType(ContentType.JSON).body("{\"onderwerp\":\"Belastingen en heffingen\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/onderwerp/KVK/12345678")
                .then().statusCode(201);

        given().contentType(ContentType.JSON).body("{\"articleId\":\"A1\",\"articleType\":\"artikel-nl\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/favoriet/KVK/12345678")
                .then().statusCode(201);

        given().when().get("/api/actualiteitenservice/v1/voorkeuren/KVK/12345678")
                .then().statusCode(200)
                .body("onderwerpen", hasSize(1))
                .body("onderwerpen[0].onderwerp", equalTo("Belastingen en heffingen"))
                .body("favorieten", hasSize(1))
                .body("favorieten[0].articleId", equalTo("A1"));
    }

    @Test
    void delete_unknownIdReturns404() {
        given()
                .when().delete("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/123456789/99999")
                .then().statusCode(404);
    }

    @Test
    void voorkeurenAreIsolatedByKoppelcode() {
        // Partij A (uses default KOPPELCODE from setUp)
        given().contentType(ContentType.JSON).body("{\"postcode\":\"1000AA\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode/BSN/111")
                .then().statusCode(201);

        // Partij B — different koppelcode
        UUID otherKoppelcode = UUID.fromString("22222222-2222-2222-2222-222222222222");
        KoppelcodeResponse kc = new KoppelcodeResponse();
        kc.koppelcode = otherKoppelcode;
        when(profielService.getKoppelcode("BSN", "222")).thenReturn(kc);

        given()
                .when().get("/api/actualiteitenservice/v1/voorkeuren/BSN/222")
                .then().statusCode(200)
                .body("postcodes", hasSize(0));
    }
}
