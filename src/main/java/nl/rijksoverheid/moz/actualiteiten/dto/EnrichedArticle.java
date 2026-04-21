package nl.rijksoverheid.moz.actualiteiten.dto;

import jakarta.validation.constraints.NotNull;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.ArticleSummary;

import java.util.List;

/**
 * Article shape the service exposes to callers: summary fields merged with the
 * subject list from the detail endpoint. One flat object per article so the
 * portaal can filter client-side when it wants to.
 */
public class EnrichedArticle {
    @NotNull
    public String identifier;
    public String headLine;
    public String additionalType;
    public String url;
    public String dateModified;
    public List<String> subjects;

    public static EnrichedArticle from(ArticleSummary summary, ArticleContract detail) {
        EnrichedArticle e = new EnrichedArticle();
        e.identifier = summary.identifier;
        e.headLine = summary.headLine;
        e.additionalType = summary.additionalType;
        e.url = summary.url;
        e.dateModified = summary.dateModified;
        e.subjects = detail != null ? detail.subjects : null;
        return e;
    }
}
