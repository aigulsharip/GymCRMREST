package com.example.gymcrmrest.repositories;

import com.example.gymcrmrest.models.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByTraineeId(Long traineeId);
}
