package com.turnoapp.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "site_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, unique = true)
    private Professional professional;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    @Column(length = 7)
    private String primaryColor;

    @Column(length = 7)
    private String secondaryColor;

    @Column(columnDefinition = "TEXT")
    private String professionalDescription;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String country;

    @Column(length = 255)
    private String businessHours;

    @Column(length = 500)
    private String welcomeMessage;

    @Column(columnDefinition = "JSON")
    private String socialMedia; // JSON string: {"instagram":"@user","facebook":"user","linkedin":"user"}

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

