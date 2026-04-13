package com.company.user_service.dto.request;

import com.company.user_service.entity.Address.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    private AddressLabel label;

    @Size(max = 150)
    private String fullName;

    private String phone;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String zipCode;

    private boolean isDefault;
}
