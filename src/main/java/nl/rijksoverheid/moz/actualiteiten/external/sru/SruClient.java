package nl.rijksoverheid.moz.actualiteiten.external.sru;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.quarkus.cache.CacheResult;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import nl.rijksoverheid.moz.actualiteiten.dto.SruPublicatie;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Fetches bekendmakingen from the Overheid.nl SRU endpoint. Same endpoint and
 * query shape the portaal consumed directly pre-Phase E.
 * <p>
 * One cache entry per postcode (TTL configured in application.properties); a
 * user with multiple postcodes reuses entries across postcodes, and different
 * users sharing a postcode share the same cache entry.
 */
@ApplicationScoped
public class SruClient {

    private static final Logger LOG = Logger.getLogger(SruClient.class);
    private static final Pattern POSTCODE = Pattern.compile("^[1-9][0-9]{3}\\s?[A-Za-z]{2}$");
    private static final Pattern POSTCODE_NOSPACE = Pattern.compile("^[1-9][0-9]{3}[A-Za-z]{2}$");
    public static final String CACHE_NAME = "sru-by-postcode";

    @ConfigProperty(name = "sru.base-url", defaultValue = "https://repository.overheid.nl/sru")
    String baseUrl;

    @ConfigProperty(name = "sru.maximum-records", defaultValue = "50")
    int maximumRecords;

    private HttpClient httpClient;
    private XmlMapper xmlMapper;

    @PostConstruct
    void init() {
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        xmlMapper = new XmlMapper();
    }

    @CacheResult(cacheName = CACHE_NAME)
    public List<SruPublicatie> fetchByPostcode(String postcode) {
        String query = "dt.spatial within /postcode \"" + postcode + "\"";
        URI uri = URI.create(baseUrl + "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&maximumRecords=" + maximumRecords);

        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                LOG.warnf("SRU fetch failed for postcode %s: status %d", postcode, response.statusCode());
                return List.of();
            }
            return parse(response.body());
        } catch (Exception e) {
            LOG.warnf("SRU fetch error for postcode %s: %s", postcode, e.getMessage());
            return List.of();
        }
    }

    private List<SruPublicatie> parse(String xml) throws Exception {
        JsonNode root = xmlMapper.readTree(xml);
        JsonNode records = root.path("records").path("record");
        if (records.isMissingNode() || records.isNull()) return List.of();

        List<SruPublicatie> out = new ArrayList<>();
        Iterator<JsonNode> it = records.isArray() ? records.elements() : List.of(records).iterator();
        while (it.hasNext()) {
            SruPublicatie p = toPublicatie(it.next());
            if (p != null) out.add(p);
        }
        return out;
    }

    private SruPublicatie toPublicatie(JsonNode record) {
        JsonNode gzd = record.path("recordData").path("gzd");
        JsonNode meta = gzd.path("originalData").path("meta");
        JsonNode owmskern = meta.path("owmskern");
        JsonNode owmsmantel = meta.path("owmsmantel");
        JsonNode tpmeta = meta.path("tpmeta");
        JsonNode enrichedData = gzd.path("enrichedData");

        SruPublicatie p = new SruPublicatie();
        p.id = textOf(owmskern.path("identifier"));
        p.title = textOf(owmskern.path("title"));
        p.type = textOf(owmskern.path("type"));
        p.creator = textOf(owmskern.path("creator"));
        p.modified = textOf(owmskern.path("modified"));
        p.abstractText = firstNonBlank(
                textOf(owmsmantel.path("abstract")),
                textOf(owmsmantel.path("alternative")),
                textOf(owmsmantel.path("description")));
        p.preferredUrl = firstNonBlank(
                textOf(enrichedData.path("preferredUrl")),
                textOf(enrichedData.path("url")));
        p.bronUrl = textOf(tpmeta.path("bronIdentifier"));
        p.productArea = textOf(tpmeta.path("product-area"));
        p.audience = textOf(owmsmantel.path("audience"));
        p.subject = textOf(owmsmantel.path("subject"));
        p.publicatienaam = textOf(tpmeta.path("publicatienaam"));

        Set<String> postcodes = new LinkedHashSet<>();
        for (JsonNode spatial : arrayOrSingle(owmskern.path("spatial"))) {
            String v = textOf(spatial);
            if (POSTCODE.matcher(v).matches()) postcodes.add(v);
        }
        String postcodeFromTpmeta = textOf(tpmeta.path("postcodeHuisnummer")).split(" ")[0];
        if (!postcodeFromTpmeta.isEmpty() && POSTCODE_NOSPACE.matcher(postcodeFromTpmeta).matches()) {
            postcodes.add(postcodeFromTpmeta);
        }
        p.postcodes = new ArrayList<>(postcodes);

        return p.id.isEmpty() && p.title.isEmpty() ? null : p;
    }

    private static Iterable<JsonNode> arrayOrSingle(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return List.of();
        if (node.isArray()) return node;
        return List.of(node);
    }

    private static String textOf(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return "";
        if (node.isTextual()) return node.asText();
        // An XML element with attributes/children deserializes as an object; the
        // actual text lives under "" or is the first value field.
        JsonNode text = node.get("");
        if (text != null && text.isTextual()) return text.asText();
        if (node.isValueNode()) return node.asText();
        return "";
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }
}
