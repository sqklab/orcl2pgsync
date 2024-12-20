package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

	Optional<UserEntity> findByUsername(String username);

	boolean existsByUsername(String username);
}

