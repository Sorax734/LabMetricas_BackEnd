package com.labMetricas.LabMetricas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/admin/test")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Este endpoint solo es accesible para ADMIN");
    }

    @GetMapping("/api/supervisor/test")
    public ResponseEntity<String> supervisorEndpoint() {
        return ResponseEntity.ok("Este endpoint es accesible para ADMIN y SUPERVISOR");
    }

    @GetMapping("/api/operador/test")
    public ResponseEntity<String> operadorEndpoint() {
        return ResponseEntity.ok("Este endpoint es accesible para ADMIN, SUPERVISOR y OPERADOR");
    }

    @GetMapping("/api/public/test")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Este es un endpoint público");
    }
} 