package com.example.gymcrmrest.services;

import com.example.gymcrmrest.models.Trainee;
import com.example.gymcrmrest.models.Trainer;
import com.example.gymcrmrest.models.Training;
import com.example.gymcrmrest.repositories.TrainerRepository;
import javax.persistence.*;
import javax.persistence.criteria.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeService trainingTypeService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainingTypeService trainingTypeService) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeService = trainingTypeService;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        log.info("Fetching all trainers");
        return trainerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepository.findById(id);
    }

    @Transactional
    public Trainer saveTrainer(Trainer trainer) {
        log.info("Saving new trainer: {}", trainer);
        trainer.setUsername(calculateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(generatePassword());
        trainer.setIsActive(true);
        trainer.setTrainingType(trainingTypeService.getTrainingTypeById(trainer.getTrainingType().getId()).orElse(null));
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer updateTrainer(Long id, Trainer updatedTrainer) {
        log.info("Updating trainer with ID {}: {}", id, updatedTrainer);
        Optional<Trainer> existingTrainerOptional = trainerRepository.findById(id);

        if (existingTrainerOptional.isPresent()) {
            Trainer existingTrainer = existingTrainerOptional.get();

            existingTrainer.setFirstName(updatedTrainer.getFirstName());
            existingTrainer.setLastName(updatedTrainer.getLastName());
            existingTrainer.setUsername(updatedTrainer.getUsername());
            existingTrainer.setPassword(updatedTrainer.getPassword());
            existingTrainer.setIsActive(updatedTrainer.getIsActive());
            existingTrainer.setTrainingType(trainingTypeService.getTrainingTypeById(updatedTrainer.getTrainingType().getId()).orElse(null));

            Trainer updated = trainerRepository.save(existingTrainer);
            log.info("Trainer with ID {} updated successfully", id);
            return updated;
        } else {
            log.warn("Trainer with ID {} not found", id);
            throw new EntityNotFoundException("Trainer with id " + id + " not found");
        }
    }

    @Transactional
    public void deleteTrainer(Long id) {
        log.info("Deleting trainer with ID: {}", id);
        trainerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username);
    }

    @Transactional
    public void updateTrainerPassword(String username, String newPassword) {
        Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);

        if (trainerOptional.isPresent()) {
            Trainer trainer = trainerOptional.get();
            trainer.setPassword(newPassword);
            trainerRepository.save(trainer);
        } else {
            throw new EntityNotFoundException("Trainer with username " + username + " not found");
        }
    }

    @Transactional
    public void updateTrainerStatus(Long id, boolean isActive) {
        Optional<Trainer> trainerOptional = trainerRepository.findById(id);

        if (trainerOptional.isPresent()) {
            Trainer trainer = trainerOptional.get();
            trainer.setIsActive(isActive);
            trainerRepository.save(trainer);
            log.info("Trainer with ID {} status updated to isActive={}", id, isActive);
        } else {
            throw new EntityNotFoundException("Trainer with ID " + id + " not found");
        }
    }

    @Transactional
    public void toggleTrainerStatus(Long traineeId) {
        Optional<Trainer> optionalTrainer = trainerRepository.findById(traineeId);

        optionalTrainer.ifPresent(trainee -> {
            trainee.setIsActive(!trainee.getIsActive());
            trainerRepository.save(trainee);
        });
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingList(String username, LocalDate fromDate, LocalDate toDate, String traineeName, String trainingTypeName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> criteriaQuery = criteriaBuilder.createQuery(Training.class);
        Root<Training> root = criteriaQuery.from(Training.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(root.join("trainer").get("username"), username));

        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }

        if (traineeName != null && !traineeName.isEmpty()) {
            Join<Training, Trainer> trainerJoin = root.join("trainee");
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(trainerJoin.get("firstName"), "%" + traineeName + "%"),
                    criteriaBuilder.like(trainerJoin.get("lastName"), "%" + traineeName + "%")
            ));
        }

        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.join("trainingType").get("trainingTypeName"), "%" + trainingTypeName + "%"));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Training> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Transactional
    public List<Trainer> getAvailableTrainersByTrainee(Optional<Trainee> traineeOptional) {
        if (traineeOptional.isPresent()) {
            Trainee trainee = traineeOptional.get();

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Trainer> criteriaQuery = criteriaBuilder.createQuery(Trainer.class);
            Root<Trainer> root = criteriaQuery.from(Trainer.class);

            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root<Training> trainingRoot = subquery.from(Training.class);
            subquery.select(trainingRoot.get("trainer").get("id"));
            subquery.where(criteriaBuilder.equal(trainingRoot.get("trainee"), trainee));

            criteriaQuery.select(root);
            criteriaQuery.where(criteriaBuilder.not(root.get("id").in(subquery)));

            TypedQuery<Trainer> query = entityManager.createQuery(criteriaQuery);
            return query.getResultList();
        } else {
            // If trainee is not present, return an empty list or handle accordingly
            return Collections.emptyList();
        }
    }


    private String calculateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String calculatedUsername = baseUsername.toLowerCase(Locale.ROOT);
        int counter = 1;

        while (trainerRepository.existsByUsername(calculatedUsername)) {
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
}
