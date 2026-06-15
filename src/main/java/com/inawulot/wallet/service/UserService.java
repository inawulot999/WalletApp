package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.KycStatus;
import com.inawulot.wallet.domain.WalletUser;
import com.inawulot.wallet.dto.CreateUserRequest;
import com.inawulot.wallet.dto.SubmitKycRequest;
import com.inawulot.wallet.dto.UpdateProfileImageRequest;
import com.inawulot.wallet.dto.UpdateProfileRequest;
import com.inawulot.wallet.dto.UpdateSecuritySettingsRequest;
import com.inawulot.wallet.exception.ComplianceException;
import com.inawulot.wallet.exception.DuplicateResourceException;
import com.inawulot.wallet.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final Map<UUID, WalletUser> users = new ConcurrentHashMap<>();
    private final Map<String, UUID> userIdsByEmail = new ConcurrentHashMap<>();

    public WalletUser createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        UUID userId = UUID.randomUUID();
        WalletUser user = new WalletUser(
                userId,
                Instant.now(),
                request.fullName().trim(),
                email,
                request.phoneNumber().trim(),
                request.country().trim().toUpperCase()
        );
        UUID previous = userIdsByEmail.putIfAbsent(email, userId);
        if (previous != null) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        users.put(userId, user);
        return user;
    }

    public WalletUser getUser(UUID userId) {
        WalletUser user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    public Collection<WalletUser> listUsers() {
        return users.values().stream()
                .sorted(Comparator.comparing(WalletUser::getCreatedAt))
                .toList();
    }

    public WalletUser submitKyc(UUID userId, SubmitKycRequest request) {
        WalletUser user = getUser(userId);
        user.submitKyc(request.bvn(), request.nin(), request.residentialAddress().trim());
        return user;
    }

    public synchronized WalletUser updateProfile(UUID userId, UpdateProfileRequest request) {
        WalletUser user = getUser(userId);
        String email = request.email().trim().toLowerCase();
        UUID existingUserId = userIdsByEmail.get(email);
        if (existingUserId != null && !existingUserId.equals(userId)) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        if (!user.getEmail().equals(email)) {
            userIdsByEmail.remove(user.getEmail(), userId);
            userIdsByEmail.put(email, userId);
        }

        user.updateProfile(
                request.fullName().trim(),
                email,
                request.phoneNumber().trim(),
                request.country().trim().toUpperCase(),
                request.residentialAddress().trim()
        );
        return user;
    }

    public WalletUser updateProfileImage(UUID userId, UpdateProfileImageRequest request) {
        WalletUser user = getUser(userId);
        user.updateProfileImage(request.profileImageUrl().trim());
        return user;
    }

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
    }
}
