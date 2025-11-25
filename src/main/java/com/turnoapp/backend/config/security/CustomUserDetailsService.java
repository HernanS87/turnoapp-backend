package com.turnoapp.backend.config.security;

import com.turnoapp.backend.model.Client;
import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.User;
import com.turnoapp.backend.model.enums.Status;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.ClientRepository;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        return createUserDetails(user);
    }

    @Transactional
    public UserDetails loadUserByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id: " + userId));

        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        if (user.getStatus() != Status.ACTIVE) {
            throw new DisabledException("User account is disabled");
        }
        Long professionalId = UserRole.PROFESSIONAL.equals(user.getRole())
                ? professionalRepository.findByUserId(user.getId())
                    .map(Professional::getId)
                    .orElse(null)
                : null;

        Long clientId = UserRole.CLIENT.equals(user.getRole())
                ? clientRepository.findByUserId(user.getId())
                .map(Client::getId)
                .orElse(null)
                : null;

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                professionalId,
                clientId
        );
    }
}
