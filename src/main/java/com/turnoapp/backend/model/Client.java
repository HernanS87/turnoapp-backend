package com.turnoapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Instant lastAccess;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Helper methods
    public String getFullName() {
        return user != null ? user.getFullName() : "";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    public String getPhone() {
        return user != null ? user.getPhone() : "";
    }
}
