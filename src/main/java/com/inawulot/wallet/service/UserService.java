package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.KycStatus;
import com.inawulot.wallet.domain.WalletStatus;
import com.inawulot.wallet.domain.WalletUser;
import com.inawulot.wallet.dto.CreateUserRequest;
import com.inawulot.wallet.dto.SubmitKycRequest;
import com.inawulot.wallet.dto.UpdateProfileImageRequest;
import com.inawulot.wallet.dto.UpdateProfileRequest;
import com.inawulot.wallet.dto.UpdateSecuritySettingsRequest;
import com.inawulot.wallet.exception.ComplianceException;
import com.inawulot.wallet.exception.DuplicateResourceException;
import com.inawulot.wallet.exception.NotFoundException;
import com.inawulot.wallet.repository.WalletUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Service
public class UserService {
    private static final String DEMO_OTP = "123456";

    private final WalletUserRepository userRepository;
    private final HashingService hashingService;
    private final InputSanitizer inputSanitizer;

    public UserService(WalletUserRepository userRepository, HashingService hashingService, InputSanitizer inputSanitizer) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.inputSanitizer = inputSanitizer;
    }

    @Transactional
    public synchronized WalletUser createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        UUID userId = UUID.randomUUID();
        WalletUser user = new WalletUser(
                userId,
                Instant.now(),
                inputSanitizer.clean(request.fullName(), 160),
                email,
                inputSanitizer.clean(request.phoneNumber(), 40),
                inputSanitizer.clean(request.country(), 2).toUpperCase(),
                hashingService.sha256("demo-passwordless:" + email),
                hashingService.sha256("1234")
        );
        return userRepository.save(user);
    }

    public WalletUser getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Collection<WalletUser> listUsers() {
        return userRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(WalletUser::getCreatedAt))
                .toList();
    }

    @Transactional
    public WalletUser submitKyc(UUID userId, SubmitKycRequest request) {
        WalletUser user = getUser(userId);
        user.submitKyc(
                inputSanitizer.clean(request.bvn(), 20),
                inputSanitizer.clean(request.nin(), 20),
                inputSanitizer.clean(request.residentialAddress(), 512)
        );
        return user;
    }

    @Transactional
    public synchronized WalletUser updateProfile(UUID userId, UpdateProfileRequest request) {
        WalletUser user = getUser(userId);
        String email = request.email().trim().toLowerCase();
        WalletUser existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        user.updateProfile(
                inputSanitizer.clean(request.fullName(), 160),
                email,
                inputSanitizer.clean(request.phoneNumber(), 40),
                inputSanitizer.clean(request.country(), 2).toUpperCase(),
                inputSanitizer.clean(request.residentialAddress(), 512)
        );
        return user;
    }

    @Transactional
    public WalletUser updateProfileImage(UUID userId, UpdateProfileImageRequest request) {
        WalletUser user = getUser(userId);
        user.updateProfileImage(inputSanitizer.clean(request.profileImageUrl(), 512));
        return user;
    }

    @Transactional
    public WalletUser updateSecuritySettings(UUID userId, UpdateSecuritySettingsRequest request) {
        WalletUser user = getUser(userId);
        user.updateSecuritySettings(
                request.twoFactorAuthenticatorEnabled(),
                request.fingerprintEnabled(),
                request.pinLockEnabled(),
                request.immediateLockEnabled()
        );
        return user;
    }

    @Transactional
    public WalletUser approveKyc(UUID userId) {
        WalletUser user = getUser(userId);
        if (user.getKycStatus() != KycStatus.SUBMITTED && user.getKycStatus() != KycStatus.VERIFIED) {
            throw new ComplianceException("KYC must be submitted before approval");
        }
        user.approveKyc();
        return user;
    }

    public void requireVerified(UUID userId) {
        WalletUser user = getUser(userId);
        if (user.getKycStatus() != KycStatus.VERIFIED) {
            throw new ComplianceException("KYC must be verified before wallet movement is allowed");
        }
        if (user.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new ComplianceException("Wallet is not active");
        }
    }

    public void requireValidTransferVerification(UUID userId, String verificationCode) {
        WalletUser user = getUser(userId);
        String code = inputSanitizer.clean(verificationCode, 16);
        boolean pinAccepted = user.getTransactionPinHash().equals(hashingService.sha256(code));
        boolean otpAccepted = user.isTwoFactorAuthenticatorEnabled() && DEMO_OTP.equals(code);
        if (!pinAccepted && !otpAccepted) {
            throw new ComplianceException("Invalid PIN or 2FA code");
        }
    }
}
