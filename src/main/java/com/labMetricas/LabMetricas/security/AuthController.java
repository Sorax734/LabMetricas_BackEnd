package com.labMetricas.LabMetricas.security;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.security.dto.AuthRequest;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken((UserDetails) authentication.getPrincipal());

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("type", "Bearer");

            ResponseObject response = new ResponseObject("Login exitoso", data, TypeResponse.SUCCESS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            ResponseObject response = new ResponseObject("Credenciales inválidas", TypeResponse.ERROR);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
} 