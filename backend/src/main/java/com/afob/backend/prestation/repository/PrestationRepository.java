package com.afob.backend.prestation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.afob.backend.prestation.entity.Prestation;
import java.util.Optional;

public interface PrestationRepository extends JpaRepository<Prestation,Long> {
    Optional<Prestation> findById(Long id);
}
