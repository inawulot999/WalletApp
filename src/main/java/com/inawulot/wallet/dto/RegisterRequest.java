package com.inawulot.wallet.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Size(max=160) String fullName, @Email @NotBlank String email, @NotBlank @Size(max=40) String phoneNumber, @NotBlank @Size(min=2,max=2) String country, @NotBlank @Size(min=12,max=128) String password) { }
