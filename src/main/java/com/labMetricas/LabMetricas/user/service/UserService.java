package com.labMetricas.LabMetricas.user.service;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.model.UserRepository;
import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ResponseEntity<ResponseObject> findAllWithRoles() {
        List<User> users = userRepository.findAll();
        
        var usersWithRoles = users.stream()
            .map(user -> {
                var userData = new java.util.HashMap<String, Object>();
                userData.put("id", user.getId());
                userData.put("name", user.getName());
                userData.put("lastname", user.getLastname());
                userData.put("email", user.getEmail());
                userData.put("phone", user.getPhone());
                userData.put("status", user.getStatus());
                userData.put("role", user.getRole().getName());
                userData.put("companyName", user.getCompanyName());
                return userData;
            })
            .collect(Collectors.toList());

        return new ResponseEntity<>(
            new ResponseObject("Users retrieved successfully", usersWithRoles, TypeResponse.SUCCESS),
            HttpStatus.OK
        );
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseEntity<ResponseObject> changePassword(ChangePasswordDto dto) {
        var userOptional = userRepository.findByEmail(dto.getEmail());
        
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(
                new ResponseObject("User not found", TypeResponse.ERROR),
                HttpStatus.NOT_FOUND
            );
        }

        var user = userOptional.get();

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return new ResponseEntity<>(
                new ResponseObject("Current password is incorrect", TypeResponse.ERROR),
                HttpStatus.BAD_REQUEST
            );
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return new ResponseEntity<>(
            new ResponseObject("Password updated successfully", TypeResponse.SUCCESS),
            HttpStatus.OK
        );
    }
} 