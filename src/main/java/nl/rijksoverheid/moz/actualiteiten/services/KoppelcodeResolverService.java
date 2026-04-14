package nl.rijksoverheid.moz.actualiteiten.services;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.KoppelcodeResponse;
import nl.rijksoverheid.moz.actualiteiten.external.profielservice.ProfielServiceClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;

/**
 * Resolves a partij's koppelcode via the Profiel Service. The Profiel Service
 * endpoint auto-creates a partij on first access, so we never see a 404 for a
 * valid BSN/KVK.
 * <p>
 * A partij's koppelcode is stable for its lifetime, so results are cached
 * in-process with a long TTL.
 */
@ApplicationScoped
public class KoppelcodeResolverService {

    public static final String CACHE_NAME = "koppelcodes";

    @Inject
    @RestClient
    ProfielServiceClient profielService;

    @CacheResult(cacheName = CACHE_NAME)
    public UUID resolve(String identificatieType, String identificatieNummer) {
        KoppelcodeResponse response = profielService.getKoppelcode(identificatieType, identificatieNummer);
        return response.koppelcode;
    }
}
