package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.external.sru.SruClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BerichtenControllerTest {

    private static final String SUBJECT = "user-berichten";

    @InjectMock
    SruClient sruClient;

    @BeforeEach
    @Transactional
    void setUp() {
        PostcodeVoorkeur.deleteAll();
    }

    @Test
    void berichten_emptyWhenNoPostcodes() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT))
                .when().get("/api/actualiteitenservice/v1/berichten")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void berichten_combinesAndDeduplicatesAcrossPostcodes() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT))
                .contentType(ContentType.JSON).body("{\"postcode\":\"1000AA\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode")
                .then().statusCode(201);
        given().header("Authorization", "Bearer " + jwt(SUBJECT))
                .contentType(ContentType.JSON).body("{\"postcode\":\"2000BB\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode")
                .then().statusCode(201);

        SruPublicatie p1 = pub("P1", "2026-04-10");
        SruPublicatie p2 = pub("P2", "2026-04-12");
        SruPublicatie p1Duplicate = pub("P1", "2026-04-10");

        when(sruClient.fetchByPostcode("1000AA")).thenReturn(List.of(p1));
        when(sruClient.fetchByPostcode("2000BB")).thenReturn(List.of(p2, p1Duplicate));

        given().header("Authorization", "Bearer " + jwt(SUBJECT))
                .when().get("/api/actualiteitenservice/v1/berichten")
                .then().statusCode(200)
                .body("$", hasSize(2))
                .body("[0].id", equalTo("P2"))
                .body("[1].id", equalTo("P1"));

        verify(sruClient).fetchByPostcode("1000AA");
        verify(sruClient).fetchByPostcode("2000BB");
    }

    @Test
    void missingBearer_returns401() {
        given().when().get("/api/actualiteitenservice/v1/berichten")
                .then().statusCode(401);
    }

    private static SruPublicatie pub(String id, String modified) {
        SruPublicatie p = new SruPublicatie();
        p.id = id;
        p.title = "Bekendmaking " + id;
        p.modified = modified;
        return p;
    }

    private static String jwt(String subject) {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = enc.encodeToString(("{\"sub\":\"" + subject + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".";
    }
}
