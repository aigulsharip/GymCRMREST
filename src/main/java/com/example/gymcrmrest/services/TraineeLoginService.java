package com.example.gymcrmrest.services;

import com.example.gymcrmrest.models.Trainee;
import com.example.gymcrmrest.repositories.TraineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TraineeLoginService {

    private final TraineeRepository traineeRepository;

    @Autowired
    public TraineeLoginService(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    public Optional<Trainee> login(String username, String password) {
        return traineeRepository.findByUsernameAndPassword(username, password);
    }


}

