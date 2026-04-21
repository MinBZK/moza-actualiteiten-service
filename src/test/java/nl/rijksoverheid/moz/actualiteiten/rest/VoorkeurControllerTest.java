package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.entity.FavorietArtikel;
import nl.rijksoverheid.moz.actualiteiten.entity.OnderwerpVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class VoorkeurControllerTest {

    private static final String SUBJECT_A = "user-a";
    private static final String SUBJECT_B = "user-b";

    @BeforeEach
    @Transactional
    void setUp() {
        PostcodeVoorkeur.deleteAll();
        OnderwerpVoorkeur.deleteAll();
        FavorietArtikel.deleteAll();
    }

    @Test
    void postcodeCrud_roundTrip() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .contentType(ContentType.JSON).body("{\"postcode\":\"1012AB\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(200)
                .body("postcodes", hasSize(1))
                .body("postcodes[0].postcode", equalTo("1012AB"));

        Long id = given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().extract().jsonPath().getLong("postcodes[0].id");

        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().delete("/api/actualiteitenservice/v1/voorkeuren/postcode/" + id)
                .then().statusCode(204);

        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(200)
                .body("postcodes", hasSize(0));
    }

    @Test
    void onderwerp_andFavoriet_alsoRoundTrip() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .contentType(ContentType.JSON).body("{\"onderwerp\":\"Belastingen en heffingen\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/onderwerp")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .contentType(ContentType.JSON).body("{\"articleId\":\"A1\",\"articleType\":\"artikel-nl\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/favoriet")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(200)
                .body("onderwerpen", hasSize(1))
                .body("onderwerpen[0].onderwerp", equalTo("Belastingen en heffingen"))
                .body("favorieten", hasSize(1))
                .body("favorieten[0].articleId", equalTo("A1"));
    }

    @Test
    void delete_unknownIdReturns404() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .when().delete("/api/actualiteitenservice/v1/voorkeuren/postcode/99999")
                .then().statusCode(404);
    }

    @Test
    void voorkeurenAreIsolatedBySubject() {
        given().header("Authorization", "Bearer " + jwt(SUBJECT_A))
                .contentType(ContentType.JSON).body("{\"postcode\":\"1000AA\"}")
                .when().post("/api/actualiteitenservice/v1/voorkeuren/postcode")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + jwt(SUBJECT_B))
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(200)
                .body("postcodes", hasSize(0));
    }

    @Test
    void missingBearer_returns401() {
        given().when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    @Test
    void garbagePayload_returns401() {
        // Valid Base64-URL header, garbage (non-JSON) payload
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String garbage = enc.encodeToString("not-json".getBytes(StandardCharsets.UTF_8));
        given().header("Authorization", "Bearer " + header + "." + garbage + ".")
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    @Test
    void invalidBase64Payload_returns401() {
        given().header("Authorization", "Bearer not.valid-base64!.sig")
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    @Test
    void missingSubClaim_returns401() {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = enc.encodeToString("{\"iss\":\"kc\"}".getBytes(StandardCharsets.UTF_8));
        given().header("Authorization", "Bearer " + header + "." + payload + ".")
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    @Test
    void blankSubClaim_returns401() {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = enc.encodeToString("{\"sub\":\"\"}".getBytes(StandardCharsets.UTF_8));
        given().header("Authorization", "Bearer " + header + "." + payload + ".")
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    @Test
    void notABearerToken_returns401() {
        given().header("Authorization", "Basic dXNlcjpwYXNz")
                .when().get("/api/actualiteitenservice/v1/voorkeuren")
                .then().statusCode(401);
    }

    private static String jwt(String subject) {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = enc.encodeToString(("{\"sub\":\"" + subject + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".";
    }
}
