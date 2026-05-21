package com.swd392.smartcarwash.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "permission")
public class Permission {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String code; // e.g. "USER_VIEW"

    @ToString.Include
    private String name; // e.g. "Xem người dùng"

    // Backward-compatible custom constructor
    public Permission(String code, String name) {
        this.code = code;
        this.name = name;
    }
} 
