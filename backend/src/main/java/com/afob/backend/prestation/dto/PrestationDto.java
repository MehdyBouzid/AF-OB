package com.afob.backend.prestation.dto;
import java.math.BigDecimal;
import com.afob.backend.prestation.entity.TypeTarification;
public record PrestationDto(
    Long id,
    String nom,
    String description,
    boolean active,
    TypeTarification typeTarification,
    BigDecimal prixMinimum
) {}
