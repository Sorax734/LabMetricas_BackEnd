package com.labMetricas.LabMetricas.user.service;

import com.labMetricas.LabMetricas.user.repository.SupervisorRepository;
import org.springframework.stereotype.Service;

@Service
public class SupervisorService {
    
    private final SupervisorRepository supervisorRepository;

    public SupervisorService(SupervisorRepository supervisorRepository) {
        this.supervisorRepository = supervisorRepository;
    }
} 