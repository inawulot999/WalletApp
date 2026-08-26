package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.PhoneLoginOtp;
import com.inawulot.wallet.domain.WalletUser;
import com.inawulot.wallet.dto.AuthResponse;
import com.inawulot.wallet.exception.ComplianceException;
import com.inawulot.wallet.repository.PhoneLoginOtpRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhoneOtpService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final PhoneLoginOtpRepository otpRepository;
    private final UserService users;
    private final PasswordEncoder passwordEncoder;
    private final SmsDeliveryService sms;
    private final SecureRandom random = new SecureRandom();

    public PhoneOtpService(PhoneLoginOtpRepository otpRepository, UserService users, PasswordEncoder passwordEncoder, SmsDeliveryService sms) {
        this.otpRepository = otpRepository;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.sms = sms;
    }

    @Transactional
    public void request(String inputPhoneNumber) {
        String phoneNumber = users.normalizePhoneNumber(inputPhoneNumber);
        WalletUser user = users.findByPhoneNumber(phoneNumber);
        if (user == null) {
            return; // Keep the response identical so phone numbers cannot be enumerated.
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        otpRepository.save(new PhoneLoginOtp(phoneNumber, passwordEncoder.encode(code), Instant.now().plus(10, ChronoUnit.MINUTES)));
        sms.sendLoginCode(phoneNumber, code);
    }

    @Transactional
    public AuthResponse verify(String inputPhoneNumber, String code) {
        String phoneNumber = users.normalizePhoneNumber(inputPhoneNumber);
        PhoneLoginOtp otp = otpRepository.findById(phoneNumber)
                .orElseThrow(() -> new ComplianceException("The code is invalid or has expired"));
        if (!otp.getExpiresAt().isAfter(Instant.now()) || otp.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            otpRepository.delete(otp);
            throw new ComplianceException("The code is invalid or has expired");
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.recordFailedAttempt();
            throw new ComplianceException("The code is invalid or has expired");
        }
        otpRepository.delete(otp);
        WalletUser user = users.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new ComplianceException("The code is invalid or has expired");
        }
        return users.authResponseFor(user);
    }
}
