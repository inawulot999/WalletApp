package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitKycRequest(
        @Pattern(regexp = "\\d{11}", message = "BVN must be 11 digits") String bvn,
        @Pattern(regexp = "\\d{11}", message = "NIN must be 11 digits") String nin,
        @NotBlank String residentialAddress
) {
}
