package com.swd392.smartcarwash.repository;


import com.swd392.smartcarwash.entity.Role;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(@NotBlank(message = "Role name must not be blank") String name);


    List<Role> findAllByNameIn(Set<String> names);
} 
