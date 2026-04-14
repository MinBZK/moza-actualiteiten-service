package nl.rijksoverheid.moz.actualiteiten.services;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.rijksoverheid.moz.actualiteiten.dto.ArticlesResponse;
import nl.rijksoverheid.moz.actualiteiten.dto.EnrichedArticle;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleSummary;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticlesContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.OndernemersPleinClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Caches the full enriched article list from Ondernemersplein and serves
 * filtered views of it in-memory.
 * <p>
 * OP's list endpoint returns summaries without subjects, so each summary is
 * paired with its detail-fetched subject list via parallel detail calls. The
 * entire enriched list is held as one cache entry; filter-combinations
 * therefore cost nothing beyond a JVM-side stream operation.
 */
@ApplicationScoped
public class ArticlesService {

    private static final Logger LOG = Logger.getLogger(ArticlesService.class);
    private static final int DETAIL_FETCH_PARALLELISM = 16;
    private static final int MAX_LIST_BATCH = 500;
    public static final String CACHE_NAME = "enriched-articles";
    private static final String SOURCE = "ondernemersplein.articles";

    @Inject
    @RestClient
    OndernemersPleinClient client;

    @Inject
    IngestionStatusService ingestionStatus;

    @Inject
    @CacheName(CACHE_NAME)
    Cache cache;

    @CacheResult(cacheName = CACHE_NAME)
    public List<EnrichedArticle> loadAllEnriched() {
        ArticlesContract list = client.getArticles(null, null, null, 0, MAX_LIST_BATCH, "modified:desc");
        List<ArticleSummary> summaries = list != null && list.articles != null ? list.articles : List.of();

        ExecutorService pool = Executors.newFixedThreadPool(DETAIL_FETCH_PARALLELISM);
        try {
            List<CompletableFuture<EnrichedArticle>> futures = summaries.stream()
                    .filter(s -> s.identifier != null)
                    .map(s -> CompletableFuture.supplyAsync(() -> enrich(s), pool))
                    .toList();
            List<EnrichedArticle> enriched = futures.stream().map(CompletableFuture::join).toList();
            ingestionStatus.recordRefresh(SOURCE);
            LOG.infof("Ondernemersplein articles refreshed: %d items", enriched.size());
            return enriched;
        } finally {
            pool.shutdown();
        }
    }

    private EnrichedArticle enrich(ArticleSummary summary) {
        ArticleContract detail = null;
        try {
            detail = client.getArticleDetail(summary.identifier);
        } catch (Exception e) {
            LOG.warnf("Article detail fetch failed for %s: %s", summary.identifier, e.getMessage());
        }
        return EnrichedArticle.from(summary, detail);
    }

    public ArticlesResponse getArticles(
            String search,
            List<String> subjects,
            List<String> types,
            Integer offset,
            Integer limit) {

        List<EnrichedArticle> all = loadAllEnriched();

        List<EnrichedArticle> filtered = all.stream()
                .filter(a -> matchesSubjects(a, subjects))
                .filter(a -> matchesTypes(a, types))
                .filter(a -> matchesSearch(a, search))
                .toList();

        int total = filtered.size();
        int from = offset != null && offset > 0 ? offset : 0;
        int size = limit != null && limit > 0 ? limit : total;
        int to = Math.min(from + size, total);
        List<EnrichedArticle> page = from < total ? filtered.subList(from, to) : List.of();

        ArticlesResponse response = new ArticlesResponse();
        response.articles = page;
        response.total = total;
        return response;
    }

    /**
     * Scheduled refresh to keep the cache warm. Runs shortly before the
     * configured TTL expires so users never hit a cold cache after startup.
     */
    @Scheduled(every = "55m", delayed = "1m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void refresh() {
        cache.invalidateAll().await().indefinitely();
        loadAllEnriched();
    }

    private static boolean matchesSubjects(EnrichedArticle a, List<String> filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (a.subjects == null || a.subjects.isEmpty()) return false;
        return a.subjects.stream().anyMatch(filter::contains);
    }

    private static boolean matchesTypes(EnrichedArticle a, List<String> filter) {
        if (filter == null || filter.isEmpty()) return true;
        return a.additionalType != null && filter.contains(a.additionalType);
    }

    private static boolean matchesSearch(EnrichedArticle a, String search) {
        if (search == null || search.isBlank()) return true;
        String needle = search.toLowerCase();
        return Objects.toString(a.headLine, "").toLowerCase().contains(needle);
    }
}
