package com.labMetricas.LabMetricas.passwordResetToken.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

public interface PasswordResetTokenInterface extends JpaRepository<PasswordResetToken, Long>{
    Optional<PasswordResetToken> findByToken(String token);

}
