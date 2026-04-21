package nl.rijksoverheid.moz.actualiteiten.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ArticlesResponse {
    @NotNull
    public List<EnrichedArticle> articles;
    public int total;
}
