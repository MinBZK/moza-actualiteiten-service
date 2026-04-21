package nl.rijksoverheid.moz.actualiteiten.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.NotAuthorizedException;

@RequestScoped
public class SubjectIdContext {

    private String subjectId;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String requireSubjectId() {
        if (subjectId == null || subjectId.isBlank()) {
            throw new NotAuthorizedException("Missing or invalid bearer token");
        }
        return subjectId;
    }
}
