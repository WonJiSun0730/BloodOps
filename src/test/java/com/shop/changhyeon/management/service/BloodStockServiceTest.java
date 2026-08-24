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
        BloodStockSyncDto initialA = new BloodStockSyncDto(0L, BloodType.A, 6625);
        BloodStockSyncDto initialO = new BloodStockSyncDto(0L, BloodType.O, 7998);
        BloodStockSyncDto initialB = new BloodStockSyncDto(0L, BloodType.B, 10258);
        BloodStockSyncDto initialAb = new BloodStockSyncDto(0L, BloodType.AB, 2942);

        bloodStockService.syncStockData(List.of(initialA, initialO, initialB, initialAb));

        assertThat(bloodStockRepository.count()).isEqualTo(4);
        assertThat(bloodStockRepository.findByCenterIdAndBloodType(0L, BloodType.A))
                .get()
                .extracting(BloodStock::getQuantity)
                .isEqualTo(6625);

        BloodStockSyncDto updatedA = new BloodStockSyncDto(0L, BloodType.A, 7000);
        bloodStockService.syncStockData(List.of(updatedA));

        assertThat(bloodStockRepository.count()).isEqualTo(4);
        assertThat(bloodStockRepository.findByCenterIdAndBloodType(0L, BloodType.A))
                .get()
                .extracting(BloodStock::getQuantity)
                .isEqualTo(7000);
    }

    @Test
    void getStocksByCenter_returnsOnlyRequestedCenterAndMapsToResponseDto() {
        bloodStockService.syncStockData(List.of(
                new BloodStockSyncDto(0L, BloodType.A, 6625),
                new BloodStockSyncDto(0L, BloodType.O, 7998),
                new BloodStockSyncDto(1L, BloodType.A, 12)
        ));

        List<BloodStockResponseDto> result = bloodStockService.getStocksByCenter(0L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(BloodStockResponseDto::getCenterId)
                .containsOnly(0L);
        assertThat(result)
                .extracting(BloodStockResponseDto::getBloodType)
                .containsExactlyInAnyOrder(BloodType.A, BloodType.O);
        assertThat(result)
                .extracting(BloodStockResponseDto::getQuantity)
                .containsExactlyInAnyOrder(6625, 7998);
    }

    @Test
    void syncStockData_rejectsNegativeQuantity() {
        BloodStockSyncDto invalid = new BloodStockSyncDto(0L, BloodType.A, -1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> bloodStockService.syncStockData(List.of(invalid)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고 수량");

        assertThat(bloodStockRepository.count()).isZero();
    }
}
