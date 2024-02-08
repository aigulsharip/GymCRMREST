package com.example.gymcrmrest.controllers.login;


import com.example.gymcrmrest.models.Trainee;
import com.example.gymcrmrest.services.TraineeLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/trainee-login")
public class TraineeLoginController {

    @Autowired
    private TraineeLoginService traineeLoginService;

    @GetMapping
    public String showLoginForm(Model model) {
        model.addAttribute("trainee", new Trainee());
        return "trainee/login";
    }

    @PostMapping("/authenticate")
    public String authenticateTrainee(Trainee trainee, Model model, HttpSession session) {
        String username = trainee.getUsername();
        String password = trainee.getPassword();

        Optional<Trainee> authenticatedTrainee = traineeLoginService.login(username, password);

        if (authenticatedTrainee.isPresent()) {
            Trainee loggedInTrainee = authenticatedTrainee.get();
            session.setAttribute("loggedInTraineeId", loggedInTrainee.getId());
            return "redirect:/trainees/edit/" + loggedInTrainee.getId();
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "trainee/login";
        }
    }

}

