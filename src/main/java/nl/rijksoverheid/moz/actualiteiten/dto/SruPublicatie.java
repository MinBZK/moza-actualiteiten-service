package nl.rijksoverheid.moz.actualiteiten.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Flat projection of a single SRU bekendmaking — shape-compatible with the
 * {@code SruPublicatie} type moza-portaal currently consumes.
 */
public class SruPublicatie {
    @NotNull
    public String id;
    @NotNull
    public String title;
    public String type;
    public String creator;
    public String modified;
    @JsonProperty("abstract")
    public String abstractText;
    public String preferredUrl;
    public String bronUrl;
    public List<String> postcodes;
    public String productArea;
    public String audience;
    public String subject;
    public String publicatienaam;
}
