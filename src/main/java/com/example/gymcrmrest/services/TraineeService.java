package com.example.gymcrmrest.services;

import com.example.gymcrmrest.models.Trainee;
import com.example.gymcrmrest.models.Trainer;
import com.example.gymcrmrest.models.Training;
import com.example.gymcrmrest.repositories.TraineeRepository;
import com.example.gymcrmrest.repositories.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TraineeService(TraineeRepository traineeRepository, TrainingRepository trainingRepository) {
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
    }

    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        log.info("Fetching all trainees");
        return traineeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeById(Long id) {
        log.info("Fetching trainee by ID: {}", id);
        return traineeRepository.findById(id);
    }

    @Transactional
    public Trainee saveTrainee(Trainee trainee) {
        log.info("Saving new trainee: {}", trainee);
        trainee.setUsername(calculateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(generatePassword());
        trainee.setIsActive(true);
        return traineeRepository.save(trainee);
    }

    @Transactional
    public Trainee updateTrainee(Long id, Trainee updatedTrainee) {
        log.info("Updating trainee with ID {}: {}", id, updatedTrainee);
        Optional<Trainee> existingTraineeOptional = traineeRepository.findById(id);

        if (existingTraineeOptional.isPresent()) {
            Trainee existingTrainee = existingTraineeOptional.get();

            // Update the fields you want to allow modification
            existingTrainee.setFirstName(updatedTrainee.getFirstName());
            existingTrainee.setLastName(updatedTrainee.getLastName());
            existingTrainee.setUsername(updatedTrainee.getUsername());
            existingTrainee.setPassword(updatedTrainee.getPassword());
            existingTrainee.setIsActive(updatedTrainee.getIsActive());
            existingTrainee.setDateOfBirth(updatedTrainee.getDateOfBirth());
            existingTrainee.setAddress(updatedTrainee.getAddress());

            Trainee updated = traineeRepository.save(existingTrainee);
            log.info("Trainee with ID {} updated successfully", id);
            return updated;
        } else {
            // Handle the case where the trainee with the given id is not found
            log.warn("Trainee with ID {} not found", id);
            throw new EntityNotFoundException("Trainee with id " + id + " not found");
        }
    }

    @Transactional
    public void toggleTraineeStatus(Long traineeId) {
        Optional<Trainee> optionalTrainee = traineeRepository.findById(traineeId);

        optionalTrainee.ifPresent(trainee -> {
            trainee.setIsActive(!trainee.getIsActive());
            traineeRepository.save(trainee);
        });
    }

    @Transactional
    public void deleteTrainee(Long traineeId) {
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with id: " + traineeId));

        List<Training> trainings = trainingRepository.findByTraineeId(traineeId);
        for (Training training : trainings) {
            trainingRepository.delete(training);
        }

        traineeRepository.delete(trainee);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeByUsername(String username) {
        log.info("Fetching trainee by username: {}", username);
        return traineeRepository.findByUsername(username);
    }

    @Transactional
    public Trainee updateTraineePassword(String username, String newPassword) {
        Optional<Trainee> traineeOptional = getTraineeByUsername(username);

        if (traineeOptional.isPresent()) {
            Trainee trainee = traineeOptional.get();
            trainee.setPassword(newPassword);
            return traineeRepository.save(trainee);
        } else {
            // Handle the case where the trainee with the given username is not found
            throw new EntityNotFoundException("Trainee with username " + username + " not found");
        }
    }

    private String calculateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String calculatedUsername = baseUsername.toLowerCase(Locale.ROOT);
        int counter = 1;

        while (traineeRepository.existsByUsername(calculatedUsername)) {
            calculatedUsername = baseUsername + counter++;
        }
        return calculatedUsername;
    }

    private String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    @Transactional
    public void deleteTraineeByUsername(String username) {
        Optional<Trainee> traineeOptional = traineeRepository.findByUsername(username);

        if (traineeOptional.isPresent()) {
            Trainee trainee = traineeOptional.get();
            traineeRepository.delete(trainee);
            log.info("Trainee with username {} deleted successfully", username);
        } else {
            throw new EntityNotFoundException("Trainee with username " + username + " not found");
        }
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingList(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> criteriaQuery = criteriaBuilder.createQuery(Training.class);
        Root<Training> root = criteriaQuery.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add username condition
        predicates.add(criteriaBuilder.equal(root.join("trainee").get("username"), username));

        // Add fromDate condition if provided
        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }

        // Add toDate condition if provided
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }

        // Add trainerName condition if provided
        if (trainerName != null && !trainerName.isEmpty()) {
            Join<Training, Trainer> trainerJoin = root.join("trainer");
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(trainerJoin.get("firstName"), "%" + trainerName + "%"),
                    criteriaBuilder.like(trainerJoin.get("lastName"), "%" + trainerName + "%")
            ));
        }

        // Add trainingTypeName condition if provided
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.join("trainingType").get("trainingTypeName"), "%" + trainingTypeName + "%"));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Training> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }


}
