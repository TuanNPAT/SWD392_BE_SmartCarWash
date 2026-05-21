package com.swd392.smartcarwash.repository;

import java.util.Optional;

import com.swd392.smartcarwash.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;




public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole_Id(Long id);
}
