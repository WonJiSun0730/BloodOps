package com.shop.changhyeon.management.service;

import com.shop.changhyeon.management.constant.BloodType;
import com.shop.changhyeon.management.dto.BloodStockResponseDto;
import com.shop.changhyeon.management.dto.BloodStockSyncDto;
import com.shop.changhyeon.management.entity.BloodStock;
import com.shop.changhyeon.management.repository.BloodStockRepository;
import com.shop.changhyeon.management.repository.BloodUnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BloodStockService.class)
class BloodStockServiceTest {

    @Autowired
    private BloodStockService bloodStockService;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Test
    void syncStockData_insertsNewStocks_andUpdatesExistingStocks() {
        BloodStockSyncDto initialA = new BloodStockSyncDto(101L, BloodType.A_POSITIVE, 12);
        BloodStockSyncDto initialO = new BloodStockSyncDto(101L, BloodType.O_POSITIVE, 24);
        BloodStockSyncDto otherCenter = new BloodStockSyncDto(102L, BloodType.A_POSITIVE, 8);

        bloodStockService.syncStockData(List.of(initialA, initialO, otherCenter));

        assertThat(bloodStockRepository.count()).isEqualTo(3);
        assertThat(bloodStockRepository.findByCenterIdAndBloodType(101L, BloodType.A_POSITIVE))
                .get()
                .extracting(BloodStock::getQuantity)
                .isEqualTo(12);

        BloodStockSyncDto updatedA = new BloodStockSyncDto(101L, BloodType.A_POSITIVE, 31);
        bloodStockService.syncStockData(List.of(updatedA));

        assertThat(bloodStockRepository.count()).isEqualTo(3);
        assertThat(bloodStockRepository.findByCenterIdAndBloodType(101L, BloodType.A_POSITIVE))
                .get()
                .extracting(BloodStock::getQuantity)
                .isEqualTo(31);
    }

    @Test
    void getStocksByCenter_returnsOnlyRequestedCenterAndMapsToResponseDto() {
        bloodStockService.syncStockData(List.of(
                new BloodStockSyncDto(101L, BloodType.A_POSITIVE, 12),
                new BloodStockSyncDto(101L, BloodType.O_NEGATIVE, 4),
                new BloodStockSyncDto(102L, BloodType.B_POSITIVE, 9)
        ));

        List<BloodStockResponseDto> result = bloodStockService.getStocksByCenter(101L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(BloodStockResponseDto::getCenterId)
                .containsOnly(101L);
        assertThat(result)
                .extracting(BloodStockResponseDto::getBloodType)
                .containsExactlyInAnyOrder(BloodType.A_POSITIVE, BloodType.O_NEGATIVE);
        assertThat(result)
                .extracting(BloodStockResponseDto::getQuantity)
                .containsExactlyInAnyOrder(12, 4);
    }

    @Test
    void syncStockData_rejectsNegativeQuantity() {
        BloodStockSyncDto invalid = new BloodStockSyncDto(101L, BloodType.A_POSITIVE, -1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> bloodStockService.syncStockData(List.of(invalid)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고 수량");

        assertThat(bloodStockRepository.count()).isZero();
    }
}
