package com.shop.changhyeon.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonorConfirmationDto {
    private String donorName;
    private String donorBirth;
    private Double height;
    private Double weight;
    private String bloodType;
    private String phone;
    private String email;
    private String address;
    private String signatureData;
}
