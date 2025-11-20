package com.turnoapp.backend.dto.auth;

import com.turnoapp.backend.model.enums.UserRole;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Long professionalId  // null si no es profesional
) {
}
