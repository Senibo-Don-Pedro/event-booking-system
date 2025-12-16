package com.senibo.userservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senibo.userservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailOrUsername(String email, String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Optional<User> findByVerificationToken(String token);
}
