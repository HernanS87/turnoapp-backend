package com.turnoapp.backend.model;

import com.turnoapp.backend.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "professionals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SiteConfig siteConfig;

    @Column(nullable = false)
    private String profession;

    @Column(nullable = false, unique = true)
    private String customUrl;

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

    public Status getStatus() {
        return user != null ? user.getStatus() : Status.INACTIVE;
    }
}
