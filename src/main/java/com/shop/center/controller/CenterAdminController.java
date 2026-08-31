package com.shop.center.controller;

import com.shop.center.dto.CenterCreateRequestDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.dto.CenterUpdateRequestDto;
import com.shop.center.service.CenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/centers")
@RequiredArgsConstructor
public class CenterAdminController {
    private final CenterService centerService;

    @GetMapping
    public Page<CenterResponseDto> getCenters(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.ASC,
                                "centerName"
                        )
                );

        return centerService.getAdminCenters(
                keyword,
                pageable
        );
    }

    @PostMapping
    public CenterResponseDto createCenter(
            @Valid @RequestBody CenterCreateRequestDto request
    ) {

        return centerService.createCenter(
                request
        );
    }

    @PutMapping("/{centerId}")
    public CenterResponseDto updateCenter(
            @PathVariable Long centerId,
            @RequestBody CenterUpdateRequestDto request
    ) {

        return centerService.updateCenter(
                centerId,
                request
        );
    }

    @DeleteMapping("/{centerId}")
    public ResponseEntity<?> deleteCenter(
            @PathVariable Long centerId
    ) {

        try {
            centerService.deleteCenter(
                    centerId
            );

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}