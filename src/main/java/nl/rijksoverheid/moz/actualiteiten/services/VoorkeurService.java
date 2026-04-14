package nl.rijksoverheid.moz.actualiteiten.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import nl.rijksoverheid.moz.actualiteiten.dto.VoorkeurenResponse;
import nl.rijksoverheid.moz.actualiteiten.entity.FavorietArtikel;
import nl.rijksoverheid.moz.actualiteiten.entity.OnderwerpVoorkeur;
import nl.rijksoverheid.moz.actualiteiten.entity.PostcodeVoorkeur;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Stores actualiteiten-specific voorkeuren (postcode, onderwerp, favoriete
 * artikelen). Data is keyed by koppelcode — BSN/KVK never lands in this
 * service's tables. The caller identifies with an {identificatieType,
 * identificatieNummer}; {@link KoppelcodeResolverService} translates that to
 * the koppelcode via Profiel Service.
 */
@ApplicationScoped
public class VoorkeurService {

    @Inject
    KoppelcodeResolverService resolver;

    public VoorkeurenResponse getAll(String type, String nummer) {
        UUID koppelcode = resolver.resolve(type, nummer);
        VoorkeurenResponse response = new VoorkeurenResponse();
        response.postcodes = PostcodeVoorkeur.listForKoppelcode(koppelcode).stream().map(v -> {
            VoorkeurenResponse.PostcodeItem item = new VoorkeurenResponse.PostcodeItem();
            item.id = v.id;
            item.postcode = v.postcode;
            return item;
        }).toList();
        response.onderwerpen = OnderwerpVoorkeur.listForKoppelcode(koppelcode).stream().map(v -> {
            VoorkeurenResponse.OnderwerpItem item = new VoorkeurenResponse.OnderwerpItem();
            item.id = v.id;
            item.onderwerp = v.onderwerp;
            return item;
        }).toList();
        response.favorieten = FavorietArtikel.listForKoppelcode(koppelcode).stream().map(v -> {
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
    public PostcodeVoorkeur addPostcode(String type, String nummer, String postcode) {
        UUID koppelcode = resolver.resolve(type, nummer);
        PostcodeVoorkeur v = new PostcodeVoorkeur();
        v.koppelcode = koppelcode;
        v.postcode = postcode;
        v.persist();
        return v;
    }

    @Transactional
    public boolean deletePostcode(String type, String nummer, Long id) {
        UUID koppelcode = resolver.resolve(type, nummer);
        return PostcodeVoorkeur.deleteByIdAndKoppelcode(id, koppelcode) > 0;
    }

    @Transactional
    public OnderwerpVoorkeur addOnderwerp(String type, String nummer, String onderwerp) {
        UUID koppelcode = resolver.resolve(type, nummer);
        OnderwerpVoorkeur v = new OnderwerpVoorkeur();
        v.koppelcode = koppelcode;
        v.onderwerp = onderwerp;
        v.persist();
        return v;
    }

    @Transactional
    public boolean deleteOnderwerp(String type, String nummer, Long id) {
        UUID koppelcode = resolver.resolve(type, nummer);
        return OnderwerpVoorkeur.deleteByIdAndKoppelcode(id, koppelcode) > 0;
    }

    @Transactional
    public FavorietArtikel addFavoriet(String type, String nummer, String articleId, String articleType) {
        UUID koppelcode = resolver.resolve(type, nummer);
        FavorietArtikel v = new FavorietArtikel();
        v.koppelcode = koppelcode;
        v.articleId = articleId;
        v.articleType = articleType;
        v.addedAt = Instant.now();
        v.persist();
        return v;
    }

    @Transactional
    public boolean deleteFavoriet(String type, String nummer, Long id) {
        UUID koppelcode = resolver.resolve(type, nummer);
        return FavorietArtikel.deleteByIdAndKoppelcode(id, koppelcode) > 0;
    }

    public List<String> getPostcodes(String type, String nummer) {
        UUID koppelcode = resolver.resolve(type, nummer);
        return PostcodeVoorkeur.listForKoppelcode(koppelcode).stream().map(v -> v.postcode).toList();
    }
}
