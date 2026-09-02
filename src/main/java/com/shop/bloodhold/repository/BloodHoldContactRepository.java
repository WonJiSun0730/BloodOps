package com.shop.bloodhold.repository;

import com.shop.bloodhold.entity.BloodHoldContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodHoldContactRepository
        extends JpaRepository<BloodHoldContact, Long> {

    boolean existsByName(String name);

    List<BloodHoldContact>
    findByLatitudeIsNullOrLongitudeIsNull();
}