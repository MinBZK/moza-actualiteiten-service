package nl.rijksoverheid.moz.actualiteiten.dto;

import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieContract;
import nl.rijksoverheid.moz.actualiteiten.external.ondernemersplein.SubsidieSummary;

import java.util.List;

public class EnrichedSubsidie {
    public String identifier;
    public String title;
    public String url;
    public String dateModified;
    public List<String> subjects;
    public List<String> regions;

    public static EnrichedSubsidie from(SubsidieSummary summary, SubsidieContract detail) {
        EnrichedSubsidie e = new EnrichedSubsidie();
        e.identifier = summary.identifier;
        e.title = summary.title;
        e.url = summary.url;
        if (detail != null) {
            e.dateModified = detail.dateModified;
            e.subjects = detail.subjects;
            e.regions = detail.regions;
        }
        return e;
    }
}
