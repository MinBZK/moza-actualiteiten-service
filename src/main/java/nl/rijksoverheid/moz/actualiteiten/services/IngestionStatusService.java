package nl.rijksoverheid.moz.actualiteiten.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks when each cached data source was last successfully refreshed from the
 * origin. The {@code ingestion-status} endpoint surfaces this to both ops and
 * the portaal's "laatst bijgewerkt" indicator.
 */
@ApplicationScoped
public class IngestionStatusService {

    private final Map<String, Instant> lastRefresh = new HashMap<>();

    public synchronized void recordRefresh(String source) {
        lastRefresh.put(source, Instant.now());
    }

    public synchronized Map<String, Instant> snapshot() {
        return new LinkedHashMap<>(lastRefresh);
    }
}
