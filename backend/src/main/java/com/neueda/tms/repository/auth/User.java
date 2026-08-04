package com.neueda.tms.repository.auth;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    private String username;

    private String passwordHash;

    @Builder.Default
    private UserRole role = UserRole.ANALYST;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum UserRole {
        ADMIN, ANALYST
    }
}
