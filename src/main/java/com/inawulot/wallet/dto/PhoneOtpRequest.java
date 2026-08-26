package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpRequest(
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,40}$", message = "Enter a valid phone number") String phoneNumber
) {
}
