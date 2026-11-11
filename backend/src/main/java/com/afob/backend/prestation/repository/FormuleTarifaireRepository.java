package com.afob.backend.prestation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.afob.backend.prestation.entity.FormuleTarifaire;

public interface FormuleTarifaireRepository extends JpaRepository<FormuleTarifaire, Long> {
}
