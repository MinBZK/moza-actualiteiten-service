package nl.rijksoverheid.moz.actualiteiten.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class VoorkeurenResponse {
    @NotNull
    public List<PostcodeItem> postcodes;
    @NotNull
    public List<OnderwerpItem> onderwerpen;
    @NotNull
    public List<FavorietItem> favorieten;

    public static class PostcodeItem {
        @NotNull
        public Long id;
        @NotNull
        public String postcode;
    }

    public static class OnderwerpItem {
        @NotNull
        public Long id;
        @NotNull
        public String onderwerp;
    }

    public static class FavorietItem {
        @NotNull
        public Long id;
        @NotNull
        public String articleId;
        @NotNull
        public String articleType;
        @NotNull
        public Instant addedAt;
    }
}
