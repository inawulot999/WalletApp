package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank String profileImageUrl
) {
}
