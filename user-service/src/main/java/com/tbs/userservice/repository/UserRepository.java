package com.tbs.userservice.repository;

import com.tbs.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Used in fetchUserEntityByEmail() to find a user by their registered email.
     * Returns an Optional to cleanly handle cases where the user doesn't exist.
     */
    Optional<User> findByEmail(String email);

    /**
     * Used in validateUserCreationPayload() and updateUserEmail()
     * to quickly check for email uniqueness without pulling the entire entity into memory.
     */
    boolean existsByEmail(String email);
}