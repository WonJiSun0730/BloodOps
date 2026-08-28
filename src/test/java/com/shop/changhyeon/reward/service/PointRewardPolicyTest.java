package com.shop.changhyeon.reward.service;

import com.shop.reservation.constant.DonationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointRewardPolicyTest {

    @Test
    void 헌혈종류별_지급포인트를_반환한다() {
        assertThat(PointRewardPolicy.pointsFor(DonationType.WHOLE_BLOOD)).isEqualTo(5_000);
        assertThat(PointRewardPolicy.pointsFor(DonationType.PLASMA)).isEqualTo(8_000);
        assertThat(PointRewardPolicy.pointsFor(DonationType.PLATELET)).isEqualTo(10_000);
        assertThat(PointRewardPolicy.pointsFor(DonationType.PLATELET_PLASMA)).isEqualTo(13_000);
    }
}
