package com.shop.changhyeon.reward.dto;

import java.util.List;

public record MyPointResponse(
        long balance,
        List<PointTransactionResponse> transactions
) {
}
