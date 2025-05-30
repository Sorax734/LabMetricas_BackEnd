package com.labMetricas.LabMetricas.security;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.security.dto.AuthRequest;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("El endpoint de prueba funciona correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody AuthRequest request) {
        try {
            logger.debug("Intento de login para el usuario: {}", request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(userDetails);

            logger.debug("Token generado exitosamente para el usuario: {}", request.getEmail());

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("type", "Bearer");
            data.put("email", userDetails.getUsername());
            data.put("roles", userDetails.getAuthorities());

            return ResponseEntity.ok(new ResponseObject("Login exitoso", data, TypeResponse.SUCCESS));
        } catch (BadCredentialsException e) {
            logger.error("Credenciales inválidas para el usuario: {}", request.getEmail());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseObject("Las credenciales no son válidas. Por favor, verifica tu email y contraseña.", TypeResponse.ERROR));
        } catch (DisabledException e) {
            logger.error("Cuenta deshabilitada para el usuario: {}", request.getEmail());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseObject("Tu cuenta está deshabilitada. Por favor, contacta al administrador.", TypeResponse.ERROR));
        } catch (LockedException e) {
            logger.error("Cuenta bloqueada para el usuario: {}", request.getEmail());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseObject("Tu cuenta está bloqueada. Por favor, contacta al administrador.", TypeResponse.ERROR));
        } catch (UsernameNotFoundException e) {
            logger.error("Usuario no encontrado: {}", request.getEmail());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseObject("No existe una cuenta con este email. Por favor, verifica tus datos.", TypeResponse.ERROR));
        } catch (Exception e) {
            logger.error("Error inesperado durante el login: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("Ha ocurrido un error inesperado. Por favor, intenta más tarde.", TypeResponse.ERROR));
        }
    }   
} 