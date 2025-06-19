package com.labMetricas.LabMetricas.security;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.security.dto.AuthRequest;
import com.labMetricas.LabMetricas.security.dto.PasswordResetRequest;
import com.labMetricas.LabMetricas.security.dto.PasswordResetConfirmRequest;
import com.labMetricas.LabMetricas.user.dto.UserDetailsDto;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.service.UserService;
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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordResetService passwordResetService;

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

            // Fetch full user details
            User user = (User) userDetailsService.loadUserByUsername(request.getEmail());
            UserDetailsDto userInfo = new UserDetailsDto(user);

            logger.debug("Token generado exitosamente para el usuario: {}", request.getEmail());

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("type", "Bearer");
            data.put("user", userInfo);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseObject> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            boolean result = passwordResetService.initiatePasswordReset(request.getEmail());
            
            if (result) {
                return ResponseEntity.ok(
                    new ResponseObject("Password reset link sent to your email", null, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("No account found with this email", null, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error during password reset initiation", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("An error occurred. Please try again later.", null, TypeResponse.ERROR));
        }
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<ResponseObject> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(
                    new ResponseObject("Token is valid", null, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("Invalid or expired token", null, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error validating reset token", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("An error occurred. Please try again later.", null, TypeResponse.ERROR));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseObject> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            // First, validate the token
            boolean isTokenValid = passwordResetService.validateResetToken(request.getToken());
            
            if (!isTokenValid) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("Invalid or expired token", null, TypeResponse.ERROR));
            }

            // If token is valid, proceed with password reset
            boolean result = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            if (result) {
                // Send confirmation email about successful password reset
                passwordResetService.sendPasswordResetConfirmationEmail(request.getToken());
                
                return ResponseEntity.ok(
                    new ResponseObject("Password reset successfully", null, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("Failed to reset password", null, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error during password reset confirmation", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("An error occurred. Please try again later.", null, TypeResponse.ERROR));
        }
    }
} 