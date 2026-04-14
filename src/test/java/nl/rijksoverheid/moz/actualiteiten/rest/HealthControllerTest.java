package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class HealthControllerTest {

    @Test
    void ping_returnsOk() {
        given()
                .when().get("/api/actualiteitenservice/v1/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("ok"));
    }
}
