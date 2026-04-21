package nl.rijksoverheid.moz.actualiteiten.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "postcode_voorkeur", indexes = @Index(name = "idx_postcode_subject_id", columnList = "subject_id"))
public class PostcodeVoorkeur extends PanacheEntity {

    @Column(name = "subject_id", nullable = false)
    public String subjectId;

    @Column(nullable = false)
    public String postcode;

    public static List<PostcodeVoorkeur> listForSubject(String subjectId) {
        return list("subjectId", subjectId);
    }

    public static long deleteByIdAndSubject(Long id, String subjectId) {
        return delete("id = ?1 AND subjectId = ?2", id, subjectId);
    }
}
