package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.auth.LoginRequest;
import com.turnoapp.backend.dto.auth.LoginResponse;
import com.turnoapp.backend.dto.auth.RegisterRequest;
import com.turnoapp.backend.model.Client;
import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.User;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.ClientRepository;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.UserRepository;
import com.turnoapp.backend.config.security.JwtTokenProvider;
import com.turnoapp.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Get user from database
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get professional ID if user is a professional
        Long professionalId = null;
        if (user.getRole() == UserRole.PROFESSIONAL) {
            professionalId = professionalRepository.findByUserId(user.getId())
                    .map(Professional::getId)
                    .orElse(null);
        }

        // Generate JWT token
        String token = tokenProvider.generateToken(user, professionalId);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                professionalId
        );
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Validate email is unique
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.CLIENT)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .build();

        user = userRepository.save(user);

        // Create client
        Client client = Client.builder()
                .user(user)
                .build();

        clientRepository.save(client);

        // Generate token
        String token = tokenProvider.generateToken(user, null);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                null
        );
    }
}
