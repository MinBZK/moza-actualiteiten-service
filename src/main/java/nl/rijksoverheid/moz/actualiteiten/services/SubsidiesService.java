package nl.rijksoverheid.moz.actualiteiten.services;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.dto.EnrichedSubsidie;
import nl.rijksoverheid.moz.actualiteiten.dto.SubsidiesResponse;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.OndernemersPleinClient;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieSummary;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidiesContract;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class SubsidiesService {

    private static final Logger LOG = Logger.getLogger(SubsidiesService.class);
    private static final int DETAIL_FETCH_PARALLELISM = 16;
    private static final int MAX_LIST_BATCH = 500;
    public static final String CACHE_NAME = "enriched-subsidies";
    private static final String SOURCE = "ondernemersplein.subsidies";

    @Inject
    @RestClient
    OndernemersPleinClient client;

    @Inject
    IngestionStatusService ingestionStatus;

    @Inject
    @CacheName(CACHE_NAME)
    Cache cache;

    @CacheResult(cacheName = CACHE_NAME)
    public List<EnrichedSubsidie> loadAllEnriched() {
        SubsidiesContract list = client.getSubsidies(null, null, 0, MAX_LIST_BATCH);
        List<SubsidieSummary> summaries = list != null && list.subsidies != null ? list.subsidies : List.of();

        ExecutorService pool = Executors.newFixedThreadPool(DETAIL_FETCH_PARALLELISM);
        try {
            List<CompletableFuture<EnrichedSubsidie>> futures = summaries.stream()
                    .filter(s -> s.identifier != null)
                    .map(s -> CompletableFuture.supplyAsync(() -> enrich(s), pool))
                    .toList();
            List<EnrichedSubsidie> enriched = futures.stream().map(CompletableFuture::join).toList();
            ingestionStatus.recordRefresh(SOURCE);
            LOG.infof("Ondernemersplein subsidies refreshed: %d items", enriched.size());
            return enriched;
        } finally {
            pool.shutdown();
        }
    }

    private EnrichedSubsidie enrich(SubsidieSummary summary) {
        SubsidieContract detail = null;
        try {
            detail = client.getSubsidieDetail(summary.identifier);
        } catch (Exception e) {
            LOG.warnf("Subsidie detail fetch failed for %s: %s", summary.identifier, e.getMessage());
        }
        return EnrichedSubsidie.from(summary, detail);
    }

    public SubsidiesResponse getSubsidies(
            List<String> subjects,
            List<String> regions,
            Integer offset,
            Integer limit) {

        List<EnrichedSubsidie> all = loadAllEnriched();

        List<EnrichedSubsidie> filtered = all.stream()
                .filter(s -> matchesSubjects(s, subjects))
                .filter(s -> matchesRegions(s, regions))
                .toList();

        int total = filtered.size();
        int from = offset != null && offset > 0 ? offset : 0;
        int size = limit != null && limit > 0 ? limit : total;
        int to = Math.min(from + size, total);
        List<EnrichedSubsidie> page = from < total ? filtered.subList(from, to) : List.of();

        SubsidiesResponse response = new SubsidiesResponse();
        response.subsidies = page;
        response.total = total;
        return response;
    }

    @Scheduled(every = "55m", delayed = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void refresh() {
        cache.invalidateAll().await().indefinitely();
        loadAllEnriched();
    }

    private static boolean matchesSubjects(EnrichedSubsidie s, List<String> filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (s.subjects == null || s.subjects.isEmpty()) return false;
        return s.subjects.stream().anyMatch(filter::contains);
    }

    private static boolean matchesRegions(EnrichedSubsidie s, List<String> filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (s.regions == null || s.regions.isEmpty()) return false;
        return s.regions.stream().anyMatch(filter::contains);
    }
}
