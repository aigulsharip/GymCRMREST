package com.example.gymcrmrest.repositories;

import com.example.gymcrmrest.models.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
}
