package com.company.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "label", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AddressLabel label = AddressLabel.HOME;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AddressLabel {
        HOME, WORK, OTHER
    }
}
