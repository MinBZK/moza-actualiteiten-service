package nl.rijksoverheid.moz.actualiteiten.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mapping from SBI 2008 code prefixes (2 digits) to relevant Ondernemersplein
 * subjects. Ported from moza-portaal
 * (src/network/ondernemersplein/sbi-subject-mapping.ts).
 *
 * <p>Subject values must match the actual {@code subjects} field on articles
 * in the Ondernemersplein OpenData API.
 */
@ApplicationScoped
public class SbiSubjectMapping {

    private static final Map<String, List<String>> SUGGESTIONS = Map.ofEntries(
            // A - Landbouw, bosbouw en visserij
            Map.entry("01", List.of("Klimaat, energie en natuur", "Exporteren")),
            Map.entry("02", List.of("Klimaat, energie en natuur")),
            Map.entry("03", List.of("Klimaat, energie en natuur", "Exporteren")),

            // C - Industrie
            Map.entry("10", List.of("Productveiligheid en verpakking", "Exporteren")),
            Map.entry("25", List.of("Productveiligheid en verpakking", "Exporteren")),
            Map.entry("26", List.of("Product, dienst en innovatie", "Exporteren")),

            // F - Bouwnijverheid
            Map.entry("41", List.of("Omgevingswet", "Verzekeringen en uitkeringen", "Arbeidsvoorwaarden")),
            Map.entry("42", List.of("Omgevingswet", "Verzekeringen en uitkeringen")),
            Map.entry("43", List.of("Omgevingswet", "Verzekeringen en uitkeringen", "Arbeidsvoorwaarden")),

            // G - Groot- en detailhandel
            Map.entry("45", List.of("Verkoopvoorwaarden en reclame")),
            Map.entry("46", List.of("Exporteren", "Importeren", "Zakelijk vervoer en logistiek")),
            Map.entry("47", List.of("Verkoopvoorwaarden en reclame")),

            // H - Vervoer en opslag
            Map.entry("49", List.of("Zakelijk vervoer en logistiek", "Energie")),
            Map.entry("52", List.of("Zakelijk vervoer en logistiek", "Importeren")),

            // I - Horeca
            Map.entry("55", List.of("Verzekeringen en uitkeringen", "Arbeidsvoorwaarden", "Personeel aannemen en inhuren")),
            Map.entry("56", List.of("Verzekeringen en uitkeringen", "Arbeidsvoorwaarden", "Personeel aannemen en inhuren")),

            // J - Informatie en communicatie
            Map.entry("62", List.of("Product, dienst en innovatie", "Bedrijf starten of overnemen")),
            Map.entry("63", List.of("Product, dienst en innovatie")),

            // K - Financiële instellingen
            Map.entry("64", List.of("Belastingen en heffingen", "Verzekeringen en uitkeringen")),
            Map.entry("66", List.of("Verzekeringen en uitkeringen")),

            // M - Specialistische zakelijke diensten
            Map.entry("69", List.of("Juridische zaken", "Administratie")),
            Map.entry("70", List.of("Bedrijfsvoering")),
            Map.entry("71", List.of("Omgevingswet", "Bedrijfsvoering")),
            Map.entry("72", List.of("Product, dienst en innovatie")),
            Map.entry("74", List.of("Product, dienst en innovatie")),

            // N - Verhuur en overige zakelijke diensten
            Map.entry("78", List.of("Personeel aannemen en inhuren", "Loon en vergoedingen")),
            Map.entry("81", List.of("Omgevingswet", "Klimaat, energie en natuur")),

            // Q - Gezondheids- en welzijnszorg
            Map.entry("86", List.of("Verzekeringen en uitkeringen", "Arbeidsomstandigheden en ziekte")),
            Map.entry("87", List.of("Arbeidsvoorwaarden", "Arbeidsomstandigheden en ziekte")),
            Map.entry("88", List.of("Arbeidsvoorwaarden", "Personeel"))
    );

    public List<String> getSuggestedSubjects(List<String> sbiCodes) {
        if (sbiCodes == null || sbiCodes.isEmpty()) {
            return List.of();
        }
        Set<String> subjects = new LinkedHashSet<>();
        for (String code : sbiCodes) {
            if (code == null || code.length() < 2) continue;
            List<String> match = SUGGESTIONS.get(code.substring(0, 2));
            if (match != null) subjects.addAll(match);
        }
        return new ArrayList<>(subjects);
    }
}
