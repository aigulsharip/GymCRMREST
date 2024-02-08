package com.example.gymcrmrest.services;

import com.example.gymcrmrest.models.Trainer;
import com.example.gymcrmrest.repositories.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainerLoginService {

    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerLoginService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public Optional<Trainer> login(String username, String password) {
        return trainerRepository.findByUsernameAndPassword(username, password);
    }
}
