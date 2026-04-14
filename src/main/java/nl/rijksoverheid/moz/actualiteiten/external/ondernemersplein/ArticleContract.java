package nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Article detail as returned by {@code GET /api/v1/articles/{id}}. Only the
 * fields we actually consume are declared; the rest are tolerated via
 * {@link JsonIgnoreProperties}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleContract {
    public String identifier;
    public String headLine;
    public String additionalType;
    public String url;
    public String dateModified;
    public List<String> subjects;
}
