package nl.rijksoverheid.moz.actualiteiten.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "favoriet_artikel", indexes = @Index(name = "idx_favoriet_koppelcode", columnList = "koppelcode"))
public class FavorietArtikel extends PanacheEntity {

    @Column(nullable = false)
    public UUID koppelcode;

    @Column(name = "article_id", nullable = false)
    public String articleId;

    @Column(name = "article_type", nullable = false)
    public String articleType;

    @Column(name = "added_at", nullable = false)
    public Instant addedAt;

    public static List<FavorietArtikel> listForKoppelcode(UUID koppelcode) {
        return list("koppelcode", koppelcode);
    }

    public static long deleteByIdAndKoppelcode(Long id, UUID koppelcode) {
        return delete("id = ?1 AND koppelcode = ?2", id, koppelcode);
    }
}
