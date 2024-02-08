package com.example.gymcrmrest.controllers;

import com.example.gymcrmrest.models.TrainingType;
import com.example.gymcrmrest.services.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/training-types")
public class TrainingTypeController {

    @Autowired
    private TrainingTypeService trainingTypeService;

    @GetMapping
    public String getAllTrainingTypes(Model model) {
        List<TrainingType> trainingTypes = trainingTypeService.getAllTrainingTypes();
        model.addAttribute("trainingTypes", trainingTypes);
        return "training-type/list";
    }
}

