package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
    
    void deleteByUserEmail(String userEmail);
}