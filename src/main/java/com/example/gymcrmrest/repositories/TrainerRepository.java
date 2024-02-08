package com.example.gymcrmrest.repositories;


import com.example.gymcrmrest.models.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    boolean existsByUsername(String username);
    Optional<Trainer> findByUsernameAndPassword (String username, String password);
    Optional<Trainer> findByUsername (String username);

}
