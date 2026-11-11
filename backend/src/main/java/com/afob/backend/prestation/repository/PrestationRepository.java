package com.afob.backend.prestation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.afob.backend.prestation.entity.Prestation;

public interface PrestationRepository extends JpaRepository<Prestation, Long> {
}
