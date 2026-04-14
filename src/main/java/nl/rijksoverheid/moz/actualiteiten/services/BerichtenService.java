package nl.rijksoverheid.moz.actualiteiten.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import nl.rijksoverheid.moz.actualiteiten.external.sru.SruClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class BerichtenService {

    @Inject
    SruClient sruClient;

    @Inject
    VoorkeurService voorkeurService;

    @Inject
    IngestionStatusService ingestionStatus;

    public List<SruPublicatie> getBerichtenForPartij(String identificatieType, String identificatieNummer) {
        List<String> postcodes = voorkeurService.getPostcodes(identificatieType, identificatieNummer);
        if (postcodes.isEmpty()) return List.of();

        List<SruPublicatie> merged = new ArrayList<>();
        Set<String> seenIds = new LinkedHashSet<>();
        for (String postcode : postcodes) {
            for (SruPublicatie p : sruClient.fetchByPostcode(postcode)) {
                String key = (p.id == null || p.id.isBlank()) ? p.preferredUrl : p.id;
                if (key != null && !key.isBlank() && seenIds.add(key)) {
                    merged.add(p);
                }
            }
        }
        ingestionStatus.recordRefresh("overheid.sru");

        merged.sort(Comparator.comparing((SruPublicatie p) ->
                p.modified == null ? "" : p.modified).reversed());
        return merged;
    }
}
