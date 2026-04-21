package nl.rijksoverheid.moz.actualiteiten.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.dto.VoorkeurenResponse;
import nl.rijksoverheid.moz.actualiteiten.entity.FavorietArtikel;
import nl.rijksoverheid.moz.actualiteiten.entity.OnderwerpVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;

import java.time.Instant;
import java.util.List;

/**
 * Stores actualiteiten-specific voorkeuren (postcode, onderwerp, favoriete
 * artikelen). Data is keyed by the Keycloak subject ID, which the controller
 * reads from the JWT the API gateway forwards.
 */
@ApplicationScoped
public class VoorkeurService {

    public VoorkeurenResponse getAll(String subjectId) {
        VoorkeurenResponse response = new VoorkeurenResponse();
        response.postcodes = PostcodeVoorkeur.listForSubject(subjectId).stream().map(v -> {
            VoorkeurenResponse.PostcodeItem item = new VoorkeurenResponse.PostcodeItem();
            item.id = v.id;
            item.postcode = v.postcode;
            return item;
        }).toList();
        response.onderwerpen = OnderwerpVoorkeur.listForSubject(subjectId).stream().map(v -> {
            VoorkeurenResponse.OnderwerpItem item = new VoorkeurenResponse.OnderwerpItem();
            item.id = v.id;
            item.onderwerp = v.onderwerp;
            return item;
        }).toList();
        response.favorieten = FavorietArtikel.listForSubject(subjectId).stream().map(v -> {
            VoorkeurenResponse.FavorietItem item = new VoorkeurenResponse.FavorietItem();
            item.id = v.id;
            item.articleId = v.articleId;
            item.articleType = v.articleType;
            item.addedAt = v.addedAt;
            return item;
        }).toList();
        return response;
    }

    @Transactional
    public PostcodeVoorkeur addPostcode(String subjectId, String postcode) {
        PostcodeVoorkeur v = new PostcodeVoorkeur();
        v.subjectId = subjectId;
        v.postcode = postcode;
        v.persist();
        return v;
    }

    @Transactional
    public boolean deletePostcode(String subjectId, Long id) {
        return PostcodeVoorkeur.deleteByIdAndSubject(id, subjectId) > 0;
    }

    @Transactional
    public OnderwerpVoorkeur addOnderwerp(String subjectId, String onderwerp) {
        OnderwerpVoorkeur v = new OnderwerpVoorkeur();
        v.subjectId = subjectId;
        v.onderwerp = onderwerp;
        v.persist();
        return v;
    }

    @Transactional
    public boolean deleteOnderwerp(String subjectId, Long id) {
        return OnderwerpVoorkeur.deleteByIdAndSubject(id, subjectId) > 0;
    }

    @Transactional
    public FavorietArtikel addFavoriet(String subjectId, String articleId, String articleType) {
        FavorietArtikel v = new FavorietArtikel();
        v.subjectId = subjectId;
        v.articleId = articleId;
        v.articleType = articleType;
        v.addedAt = Instant.now();
        v.persist();
        return v;
    }

    @Transactional
    public boolean deleteFavoriet(String subjectId, Long id) {
        return FavorietArtikel.deleteByIdAndSubject(id, subjectId) > 0;
    }

    public List<String> getPostcodes(String subjectId) {
        return PostcodeVoorkeur.listForSubject(subjectId).stream().map(v -> v.postcode).toList();
    }
}
