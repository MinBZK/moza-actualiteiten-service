package nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Subsidy detail as returned by {@code GET /api/v1/subsidies/{id}}. Only the
 * fields we actually consume are declared.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsidieContract {
    public String identifier;
    public String title;
    public String url;
    public String dateModified;
    public List<String> subjects;
    public List<String> regions;
}
