package com.shop.reservation.controller;

import com.shop.reservation.exception.ActiveReservationExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        assignableTypes = ReservationApiController.class
)
public class ReservationExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>>
    handleIllegalStateException(
            IllegalStateException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(ActiveReservationExistsException.class)
    public ResponseEntity<Map<String, Object>>
    handleActiveReservationExistsException(
            ActiveReservationExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "code",
                                "ACTIVE_RESERVATION_EXISTS",
                                "message",
                                exception.getMessage(),
                                "existingReservation",
                                exception.getExistingReservation()
                        )
                );
    }
}