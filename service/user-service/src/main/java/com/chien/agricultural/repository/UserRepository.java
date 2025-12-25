package com.chien.agricultural.repository;

import com.chien.agricultural.model.User;
import com.chien.agricultural.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByPhone(String phoneNumber);

    boolean existsByPhone(String phoneNumber);

    Page<User> findAllByUserRoleNot(UserRole role, Pageable pageable);

    List<User> findAllByUserRole(UserRole userRole);
}
