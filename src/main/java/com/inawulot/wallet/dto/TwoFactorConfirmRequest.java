package com.inawulot.wallet.dto;
import jakarta.validation.constraints.Pattern;
public record TwoFactorConfirmRequest(@Pattern(regexp = "^[0-9]{6}$", message = "Enter the 6-digit authenticator code") String code) { }
