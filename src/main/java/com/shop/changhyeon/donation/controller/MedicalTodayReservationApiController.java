package com.shop.changhyeon.donation.controller;

import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<AdminReservationResponseDto> getTodayReservations() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return reservationRepository.findBySlot_ReservationDateOrderBySlot_StartTimeAscIdAsc(today)
                .stream().map(AdminReservationResponseDto::new).toList();
    }
}
