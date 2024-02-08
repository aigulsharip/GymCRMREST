package com.example.gymcrmrest.controllers;

import com.example.gymcrmrest.models.Trainee;
import com.example.gymcrmrest.models.Trainer;
import com.example.gymcrmrest.models.Training;
import com.example.gymcrmrest.services.TraineeService;
import com.example.gymcrmrest.services.TrainerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trainees")
public class TraineeController {

    private final TraineeService traineeService;

    private final TrainerService trainerService;

    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @GetMapping("/check")
    public String check () {
        return "403";
    }


    @GetMapping
    public String getAllTrainees(Model model, @RequestParam(name = "search", required = false) String search) {
        List<Trainee> trainees;

        if (search != null && !search.isEmpty()) {
            Optional<Trainee> trainee = traineeService.getTraineeByUsername(search);
            if (trainee.isPresent()) {
                model.addAttribute("trainee", trainee.get());
                return "trainee/edit";
            } else {
                trainees = traineeService.getAllTrainees();
                model.addAttribute("trainees", trainees);
                model.addAttribute("search", search);
                model.addAttribute("searchMessage", "No trainee found for username: " + search);
                return "trainee/list";
            }
        } else {
            trainees = traineeService.getAllTrainees();
            model.addAttribute("trainees", trainees);
            return "trainee/list";
        }
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("trainee", new Trainee());
        return "trainee/add";
    }

    @PostMapping("/add")
    public String addTrainee(@Valid @ModelAttribute Trainee trainee, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("trainee", trainee);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "trainee/add"; // Assuming trainee_form is your form view
        }
        traineeService.saveTrainee(trainee);
        return "redirect:/trainees";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Trainee> trainee = traineeService.getTraineeById(id);
        model.addAttribute("trainee", trainee.orElse(new Trainee()));
        return "trainee/edit";
    }

    @PostMapping("/edit/{id}")
    public String editTrainee(@PathVariable Long id, @Valid @ModelAttribute Trainee trainee) {
        trainee.setId(id);
        traineeService.saveTrainee(trainee);
        return "redirect:/trainees";
    }

    @GetMapping("/delete/{id}")
    public String deleteTrainee(@PathVariable Long id) {
        traineeService.deleteTrainee(id);
        return "redirect:/trainees";
    }

    @GetMapping("/profile/{username}")
    public String viewTraineeProfile(@PathVariable String username, Model model) {
        Optional<Trainee> trainee = traineeService.getTraineeByUsername(username);
        if (trainee.isPresent()) {
            model.addAttribute("trainee", trainee.get());
            return "trainee/edit";
        } else {
            return "redirect:/trainees"; // Redirect to the trainee list or another appropriate page
        }
    }

    @PostMapping("/profile/{username}/change-password")
    public String changePassword(@PathVariable String username, @RequestParam String newPassword, Model model) {
        Trainee trainee = traineeService.getTraineeByUsername(username).orElse(null);
        try {
            traineeService.updateTraineePassword(username, newPassword);
            return "redirect:/trainees/edit/" + trainee.getId() + "?successMessage=Password changed successfully";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to change password. Please try again.");
            return "redirect:/trainees/edit/" + trainee.getId() + "?errorMessage=Failed to change password. Please try again.";
        }
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleTraineeStatus(@PathVariable Long id) {
        traineeService.toggleTraineeStatus(id);
        return "redirect:/trainees";
    }

    @GetMapping("/delete-by-username/{username}")
    public String deleteTraineeByUsername(@PathVariable String username) {
        traineeService.deleteTraineeByUsername(username);
        return "redirect:/trainees";
    }

    @GetMapping("/training-list")
    public String getTraineeTrainingList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingTypeName,
            Model model
    ) {
        List<Training> trainingList = traineeService.getTraineeTrainingList(username, fromDate, toDate, trainerName, trainingTypeName);
        model.addAttribute("trainingList", trainingList);
        List<Trainee> trainees = traineeService.getAllTrainees();
        model.addAttribute("trainees", trainees);
        model.addAttribute("trainee", new Trainee());  // Add an empty trainee object for binding
        model.addAttribute("trainer", new Trainer());

        Optional<Trainee> trainee = traineeService.getTraineeByUsername(username);
        List<Trainer> availableTrainers = trainerService.getAvailableTrainersByTrainee(trainee);
        model.addAttribute("availableTrainers", availableTrainers);
        return "trainee/training-list";
    }

}

