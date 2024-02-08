package com.example.gymcrmrest.controllers;

import com.example.gymcrmrest.models.Trainer;
import com.example.gymcrmrest.models.Training;
import com.example.gymcrmrest.models.TrainingType;
import com.example.gymcrmrest.services.TrainerService;
import com.example.gymcrmrest.services.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainingTypeService trainingTypeService;

    @GetMapping
    public String getAllTrainers(Model model, @RequestParam(name = "search", required = false) String search) {
        List<Trainer> trainers;

        if (search != null && !search.isEmpty()) {
            Optional<Trainer> trainer = trainerService.getTrainerByUsername(search);

            if (trainer.isPresent()) {
                model.addAttribute("trainer", trainer.get());
                return "trainer/edit";
            } else {
                trainers = trainerService.getAllTrainers();
                model.addAttribute("trainers", trainers);
                model.addAttribute("search", search);
                model.addAttribute("searchMessage", "No trainer found for username: " + search);
                return "trainer/list";
            }
        } else {
            trainers = trainerService.getAllTrainers();
            model.addAttribute("trainers", trainers);
            return "trainer/list";
        }
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<TrainingType> trainingTypes = trainingTypeService.getAllTrainingTypes();
        model.addAttribute("trainer", new Trainer());
        model.addAttribute("trainingTypes", trainingTypes);
        return "trainer/add";
    }

    @PostMapping("/add")
    public String addTrainer(@ModelAttribute Trainer trainer) {
        trainerService.saveTrainer(trainer);
        return "redirect:/trainers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Trainer> trainer = trainerService.getTrainerById(id);
        List<TrainingType> trainingTypes = trainingTypeService.getAllTrainingTypes();
        model.addAttribute("trainer", trainer.orElse(new Trainer()));
        model.addAttribute("trainingTypes", trainingTypes);
        return "trainer/edit";
    }

    @PostMapping("/edit/{id}")
    public String editTrainer(@PathVariable Long id, @ModelAttribute Trainer trainer) {
        trainer.setId(id);
        trainerService.updateTrainer(id, trainer);
        return "redirect:/trainers";
    }

    @GetMapping("/delete/{id}")
    public String deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return "redirect:/trainers";
    }

    @GetMapping("/profile/{username}")
    public String viewTrainerProfile(@PathVariable String username, Model model) {
        Optional<Trainer> trainer = trainerService.getTrainerByUsername(username);

        if (trainer.isPresent()) {
            model.addAttribute("trainer", trainer.get());
            return "trainer/edit";
        } else {
            return "redirect:/trainers"; // Redirect to the trainee list or another appropriate page
        }
    }

    @PostMapping("/profile/{username}/change-password")
    public String changePassword(@PathVariable String username, @RequestParam String newPassword, Model model) {
        Trainer trainer = trainerService.getTrainerByUsername(username).orElse(null);
        try {
            trainerService.updateTrainerPassword(username, newPassword);
            model.addAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to change password. Please try again.");
        }

        return "redirect:/trainers/edit/" + trainer.getId() + "?successMessage=Password changed successfully!";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleTraineeStatus(@PathVariable Long id) {
        trainerService.toggleTrainerStatus(id);
        return "redirect:/trainers";
    }

    @GetMapping("/training-list")
    public String getTrainerTrainingList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName,
            @RequestParam(required = false) String trainingTypeName,
            Model model
    ) {
        // Logging to check parameter values
        System.out.println("Username: " + username);
        System.out.println("FromDate: " + fromDate);
        System.out.println("ToDate: " + toDate);
        System.out.println("TraineeName: " + traineeName);
        System.out.println("TrainingTypeName: " + trainingTypeName);

        List<Training> trainingList = trainerService.getTrainerTrainingList(username, fromDate, toDate, traineeName, trainingTypeName);

        // Logging to check the size of the trainingList
        System.out.println("TrainingList size: " + trainingList.size());

        model.addAttribute("trainingList", trainingList);
        model.addAttribute("trainers", trainerService.getAllTrainers());
        model.addAttribute("trainer", new Trainer());  // Add an empty trainer object for binding
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("traineeName", traineeName);
        model.addAttribute("trainingTypeName", trainingTypeName);

        return "trainer/training-list";
    }


}
