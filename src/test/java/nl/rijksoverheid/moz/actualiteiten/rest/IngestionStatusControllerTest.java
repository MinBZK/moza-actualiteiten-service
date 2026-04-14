package nl.rijksoverheid.moz.actualiteiten.rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.services.IngestionStatusService;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class IngestionStatusControllerTest {

    @Inject
    IngestionStatusService service;

    @Test
    void status_reflectsRecordedRefreshes() {
        service.recordRefresh("test.source");

        given()
                .when().get("/api/actualiteitenservice/v1/ingestion-status")
                .then()
                .statusCode(200)
                .body("'test.source'", notNullValue());
    }
}
