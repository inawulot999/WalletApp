package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.AuthResponse;
import com.inawulot.wallet.dto.LoginRequest;
import com.inawulot.wallet.dto.PasswordResetConfirmRequest;
import com.inawulot.wallet.dto.PasswordResetRequest;
import com.inawulot.wallet.dto.PhoneOtpRequest;
import com.inawulot.wallet.dto.PhoneOtpVerifyRequest;
import com.inawulot.wallet.dto.RegisterRequest;
import com.inawulot.wallet.service.PasswordResetEmailService;
import com.inawulot.wallet.service.PhoneOtpService;
import com.inawulot.wallet.service.RateLimitService;
import com.inawulot.wallet.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
public class AuthController {
    private final UserService users;
    private final RateLimitService limits;
    private final PasswordResetEmailService resetEmails;
    private final PhoneOtpService phoneOtps;

    public AuthController(UserService users, RateLimitService limits, PasswordResetEmailService resetEmails, PhoneOtpService phoneOtps) {
        this.users = users;
        this.limits = limits;
        this.resetEmails = resetEmails;
        this.phoneOtps = phoneOtps;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":auth");
        return users.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":auth");
        return users.login(request);
    }

    @PostMapping("/phone-otp/request")
    public Map<String, String> requestPhoneOtp(@Valid @RequestBody PhoneOtpRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":phone-otp");
        phoneOtps.request(request.phoneNumber());
        return Map.of("message", "If that phone number belongs to an account, a sign-in code has been sent.");
    }

    @PostMapping("/phone-otp/verify")
    public AuthResponse verifyPhoneOtp(@Valid @RequestBody PhoneOtpVerifyRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":phone-otp");
        return phoneOtps.verify(request.phoneNumber(), request.code());
    }

    @PostMapping("/password-reset/request")
    public Map<String, String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":password-reset");
        UserService.PasswordResetToken reset = users.createPasswordResetToken(request.email());
        if (reset != null) {
            resetEmails.send(reset.user(), reset.value());
        }
        return Map.of("message", "If an account exists for that email, a password reset link has been sent.");
    }

    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request, HttpServletRequest http) {
        limits.check(http.getRemoteAddr() + ":password-reset");
        users.resetPassword(request.token(), request.newPassword());
        return Map.of("message", "Password reset successful. You can now log in.");
    }
}
