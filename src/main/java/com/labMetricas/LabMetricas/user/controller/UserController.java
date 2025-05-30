package com.labMetricas.LabMetricas.user.controller;

import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.user.service.UserService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/with-roles")
    public ResponseEntity<ResponseObject> getUsersWithRoles() {
        return userService.findAllWithRoles();
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        return userService.changePassword(dto);
    }


} 