package com.company.user_service.repository;

import com.company.user_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(UUID userId);

    Optional<Address> findByIdAndUserId(Long id, UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void clearDefaultForUser(UUID userId);
}
