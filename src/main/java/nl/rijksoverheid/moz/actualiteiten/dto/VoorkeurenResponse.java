package nl.rijksoverheid.moz.actualiteiten.dto;

import java.time.Instant;
import java.util.List;

public class VoorkeurenResponse {
    public List<PostcodeItem> postcodes;
    public List<OnderwerpItem> onderwerpen;
    public List<FavorietItem> favorieten;

    public static class PostcodeItem {
        public Long id;
        public String postcode;
    }

    public static class OnderwerpItem {
        public Long id;
        public String onderwerp;
    }

    public static class FavorietItem {
        public Long id;
        public String articleId;
        public String articleType;
        public Instant addedAt;
    }
}
