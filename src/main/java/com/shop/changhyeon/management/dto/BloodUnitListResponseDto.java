package com.shop.changhyeon.management.dto;

import java.util.List;

public record BloodUnitListResponseDto(
        List<BloodUnitResponseDto> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        long available,
        long expiringSoon,
        long disposedOrHeld
) {
}
