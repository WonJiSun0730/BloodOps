package com.shop.center.repository;

import com.shop.center.entity.Center;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CenterRepository extends JpaRepository<Center, Long> {

    List<Center> findByCenterNameContainingIgnoreCase(String centerName);

    List<Center> findByAddressContainingIgnoreCase(String address);


    Page<Center> findByCenterNameContainingIgnoreCase(
            String centerName,
            Pageable pageable
    );

    Page<Center> findByAddressContainingIgnoreCase(
            String address,
            Pageable pageable
    );

    List<Center> findByActiveTrue();

    Optional<Center> findByCenterNameAndAddress(
            String centerName,
            String address //센터 이름 + 주소 조합으로 안전하게 동일 센터 판단
    );

    List<Center> findByLatitudeIsNullOrLongitudeIsNull();
}