package nl.rijksoverheid.moz.actualiteiten.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Extracts the subject ID claim from the {@code Authorization: Bearer ...} JWT
 * and populates {@link SubjectIdContext} for the request. Auth validation
 * itself is handled upstream by the API gateway; this filter only decodes the
 * payload to read the subject claim.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SubjectIdFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SubjectIdFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ConfigProperty(name = "auth.subject-claim-name", defaultValue = "sub")
    String claimName;

    @Inject
    SubjectIdContext subjectIdContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String auth = ctx.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
            return;
        }
        String token = auth.substring(BEARER_PREFIX.length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            LOG.debugf("Bearer token is not a JWT (has %d parts)", parts.length);
            return;
        }

        byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            LOG.debugf("JWT payload is not valid Base64-URL: %s", e.getMessage());
            return;
        }

        JsonNode node;
        try {
            node = MAPPER.readTree(new String(payload, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            LOG.warnf("JWT payload is not valid JSON: %s", e.getMessage());
            return;
        }

        JsonNode claim = node.get(claimName);
        if (claim != null && !claim.isNull()) {
            subjectIdContext.setSubjectId(claim.asText());
        }
    }
}
