package nl.rijksoverheid.moz.actualiteiten.services;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SbiSubjectMappingTest {

    private final SbiSubjectMapping mapping = new SbiSubjectMapping();

    @Test
    void horecaCodeMapsToHorecaSubjects() {
        List<String> subjects = mapping.getSuggestedSubjects(List.of("5610"));

        assertTrue(subjects.contains("Arbeidsvoorwaarden"));
        assertTrue(subjects.contains("Personeel aannemen en inhuren"));
    }

    @Test
    void multipleCodesMergeDeduplicated() {
        // 41 and 43 both map to "Omgevingswet", should appear only once
        List<String> subjects = mapping.getSuggestedSubjects(List.of("4110", "4321"));

        long omgevingswetCount = subjects.stream().filter("Omgevingswet"::equals).count();
        assertEquals(1, omgevingswetCount);
    }

    @Test
    void unknownCodeReturnsEmpty() {
        assertEquals(List.of(), mapping.getSuggestedSubjects(List.of("9999")));
    }

    @Test
    void nullOrEmptyInputReturnsEmpty() {
        assertEquals(List.of(), mapping.getSuggestedSubjects(null));
        assertEquals(List.of(), mapping.getSuggestedSubjects(List.of()));
    }

    @Test
    void shortCodeIsSkipped() {
        assertEquals(List.of(), mapping.getSuggestedSubjects(List.of("5")));
    }
}
