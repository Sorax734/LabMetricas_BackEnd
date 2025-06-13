package com.labMetricas.LabMetricas.user.controller;

import com.labMetricas.LabMetricas.user.service.SupervisorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {
    
    private final SupervisorService supervisorService;

    public SupervisorController(SupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }
} 