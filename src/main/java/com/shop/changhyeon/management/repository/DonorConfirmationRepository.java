package com.shop.changhyeon.management.repository;

import com.shop.changhyeon.management.entity.DonorConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DonorConfirmationRepository extends JpaRepository<DonorConfirmation, Long> {
    Optional<DonorConfirmation> findFirstByNameAndResidentNumberHash(String name, String residentNumberHash);
}
