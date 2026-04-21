package nl.rijksoverheid.moz.actualiteiten.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "favoriet_artikel", indexes = @Index(name = "idx_favoriet_subject_id", columnList = "subject_id"))
public class FavorietArtikel extends PanacheEntity {

    @Column(name = "subject_id", nullable = false)
    public String subjectId;

    @Column(name = "article_id", nullable = false)
    public String articleId;

    @Column(name = "article_type", nullable = false)
    public String articleType;

    @Column(name = "added_at", nullable = false)
    public Instant addedAt;

    public static List<FavorietArtikel> listForSubject(String subjectId) {
        return list("subjectId", subjectId);
    }

    public static long deleteByIdAndSubject(Long id, String subjectId) {
        return delete("id = ?1 AND subjectId = ?2", id, subjectId);
    }
}
