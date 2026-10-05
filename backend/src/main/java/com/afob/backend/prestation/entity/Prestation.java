package com.afob.backend.prestation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prestation")
public class Prestation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type_tarification", columnDefinition = "tarif", nullable = false)
    private TypeTarification typeTarification;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "prix_minimum", precision = 10, scale = 2)
    private BigDecimal prixMinimum;

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public TypeTarification getTypeTarification() {
        return typeTarification;
    }

    public void setTypeTarification(TypeTarification typeTarification) {
        this.typeTarification = typeTarification;
    }

    public BigDecimal getPrixMinimum() {
        return prixMinimum;
    }

    public void setPrixMinimum(BigDecimal prixMinimum) {
        this.prixMinimum = prixMinimum;
    }

}
