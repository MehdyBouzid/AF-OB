package com.afob.backend.prestation.dto;

import java.math.BigDecimal;
import com.afob.backend.prestation.entity.TypeTarification;

public record PrestationRequestDto(
                String nom,
                String description,
                TypeTarification typeTarification,
                BigDecimal prixMinimum) {
}
