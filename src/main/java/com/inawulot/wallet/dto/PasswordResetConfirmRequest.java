package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{8}$", message = "Enter the 8-digit reset code") String token,
        @NotBlank @Size(min = 12, max = 128) String newPassword
) { }
