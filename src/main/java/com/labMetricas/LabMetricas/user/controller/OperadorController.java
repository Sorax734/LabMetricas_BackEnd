package com.labMetricas.LabMetricas.user.controller;

import com.labMetricas.LabMetricas.user.service.OperadorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operador")
public class OperadorController {
    
    private final OperadorService operadorService;

    public OperadorController(OperadorService operadorService) {
        this.operadorService = operadorService;
    }
} 