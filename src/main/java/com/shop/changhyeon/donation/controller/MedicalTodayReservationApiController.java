package com.shop.changhyeon.donation.controller;

import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/medical/reservations")
@RequiredArgsConstructor
public class MedicalTodayReservationApiController {
    private final ReservationRepository reservationRepository;

    @GetMapping("/today")
    public List<AdminReservationResponseDto> getTodayReservations(
            @RequestParam(name = "centerId", required = false) Long centerId
    ) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<Reservation> reservations =
                reservationRepository.findMedicalTodayReservations(
                        today,
                        centerId
                );

        return reservations.stream()
                .map(AdminReservationResponseDto::new)
                .toList();
    }
}
