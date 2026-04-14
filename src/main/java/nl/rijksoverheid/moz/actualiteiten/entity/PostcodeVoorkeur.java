package nl.rijksoverheid.moz.actualiteiten.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "postcode_voorkeur", indexes = @Index(name = "idx_postcode_koppelcode", columnList = "koppelcode"))
public class PostcodeVoorkeur extends PanacheEntity {

    @Column(nullable = false)
    public UUID koppelcode;

    @Column(nullable = false)
    public String postcode;

    public static List<PostcodeVoorkeur> listForKoppelcode(UUID koppelcode) {
        return list("koppelcode", koppelcode);
    }

    public static long deleteByIdAndKoppelcode(Long id, UUID koppelcode) {
        return delete("id = ?1 AND koppelcode = ?2", id, koppelcode);
    }
}
