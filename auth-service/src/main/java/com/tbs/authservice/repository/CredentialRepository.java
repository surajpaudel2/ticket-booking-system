package com.tbs.authservice.repository;

import com.tbs.authservice.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByEmail(String email);

    boolean existsByEmail(String email);
}
