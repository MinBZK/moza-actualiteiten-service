package nl.rijksoverheid.moz.actualiteiten.dto;

import java.util.List;

/**
 * Flat projection of a single SRU bekendmaking — shape-compatible with the
 * {@code SruPublicatie} type moza-portaal currently consumes.
 */
public class SruPublicatie {
    public String id;
    public String title;
    public String type;
    public String creator;
    public String modified;
    public String abstractText;
    public String preferredUrl;
    public String bronUrl;
    public List<String> postcodes;
    public String productArea;
    public String audience;
    public String subject;
    public String publicatienaam;
}
