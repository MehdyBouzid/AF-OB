package com.afob.backend.prestation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "formule_tarifaire")
@Getter
@Setter
public class FormuleTarifaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prix")
    private Long id;

    @Column(nullable = false)
    private Integer prix;

    @Column(nullable = false, length = 255)
    private String libelle;

    @ManyToOne
    @JoinColumn(nullable = false, name = "prestation_id", foreignKey = @ForeignKey(name = "prestation_id_Fk"))
    private Prestation prestation;

}
