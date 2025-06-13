package com.labMetricas.LabMetricas.user.service;

import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.OperadorRepository;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OperadorService {
    
    private final OperadorRepository operadorRepository;
    private final UserRepository userRepository;

    public OperadorService(OperadorRepository operadorRepository, UserRepository userRepository) {
        this.operadorRepository = operadorRepository;
        this.userRepository = userRepository;
    }

    public List<User> findAllOperadores() {
        return operadorRepository.findAll();
    }

    public Optional<User> findOperadorById(UUID id) {
        return operadorRepository.findById(id);
    }
} 