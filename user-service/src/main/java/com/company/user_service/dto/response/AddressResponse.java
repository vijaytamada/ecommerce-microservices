package com.company.user_service.dto.response;

import com.company.user_service.entity.Address.AddressLabel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private AddressLabel label;
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
