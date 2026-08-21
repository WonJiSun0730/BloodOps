package com.stock.service;

import com.stock.dto.BloodStockResponseDto;
import com.stock.dto.BloodStockSyncDto;
import com.stock.entity.BloodStock;
import com.stock.repository.BloodStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BloodStockService {

    private final BloodStockRepository bloodStockRepository;

    @Transactional
    public void syncStockData(List<BloodStockSyncDto> dtoList) {
        for (BloodStockSyncDto dto : dtoList) {
            BloodStock stock = bloodStockRepository.findByCenterIdAndBloodType(dto.getCenterId(), dto.getBloodType())
                    .orElseGet(() -> BloodStock.builder()
                            .centerId(dto.getCenterId())
                            .bloodType(dto.getBloodType())
                            .quantity(0)
                            .build());

            stock.updateQuantity(dto.getQuantity());
            bloodStockRepository.save(stock);
        }
    }

    public List<BloodStockResponseDto> getStocksByCenter(Long centerId) {
        return bloodStockRepository.findByCenterId(centerId)
                .stream()
                .map(BloodStockResponseDto::from)
                .collect(Collectors.toList());
    }
}