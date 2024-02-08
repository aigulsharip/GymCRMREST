package com.example.gymcrmrest.services;


import com.example.gymcrmrest.models.Training;
import com.example.gymcrmrest.repositories.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;

    @Autowired
    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        log.info("Fetching all trainings");
        return trainingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Training> getTrainingById(Long id) {
        return trainingRepository.findById(id);
    }

    @Transactional
    public Training saveTraining(Training training) {
        log.info("Saving new training: {}", training);
        return trainingRepository.save(training);
    }

    @Transactional
    public Training updateTraining(Long id, Training updatedTraining) {
        log.info("Updating training with ID {}: {}", id, updatedTraining);
        Optional<Training> existingTrainingOptional = trainingRepository.findById(id);

        if (existingTrainingOptional.isPresent()) {
            Training existingTraining = existingTrainingOptional.get();

            // Update the fields you want to allow modification
            existingTraining.setTrainee(updatedTraining.getTrainee());
            existingTraining.setTrainer(updatedTraining.getTrainer());
            existingTraining.setTrainingType(updatedTraining.getTrainingType());
            existingTraining.setTrainingName(updatedTraining.getTrainingName());
            existingTraining.setTrainingDate(updatedTraining.getTrainingDate());
            existingTraining.setTrainingDuration(updatedTraining.getTrainingDuration());

            Training updated = trainingRepository.save(existingTraining);
            log.info("Training with ID {} updated successfully", id);
            return updated;
        } else {
            // Handle the case where the training with the given id is not found
            log.warn("Training with ID {} not found", id);
            // You may throw an exception or handle it according to your application's logic
            // For simplicity, I'm returning null here
            return null;
        }
    }
    @Transactional
    public void deleteTraining(Long id) {
        log.info("Deleting training with ID: {}", id);
        trainingRepository.deleteById(id);
    }
}
