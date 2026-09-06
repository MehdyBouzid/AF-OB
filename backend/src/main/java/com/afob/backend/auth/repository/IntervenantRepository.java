package com.afob.backend.auth.repository;

import com.afob.backend.auth.entity.Intervenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IntervenantRepository extends JpaRepository<Intervenant, Long> {
    Optional<Intervenant> findByEmail(String email);
}