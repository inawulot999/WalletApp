package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpVerifyRequest(
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,40}$", message = "Enter a valid phone number") String phoneNumber,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Enter the 6-digit code") String code
) {
}
