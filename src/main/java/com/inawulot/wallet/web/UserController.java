package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.CreateUserRequest;
import com.inawulot.wallet.dto.SubmitKycRequest;
import com.inawulot.wallet.dto.UpdateProfileImageRequest;
import com.inawulot.wallet.dto.UpdateProfileRequest;
import com.inawulot.wallet.dto.UpdateSecuritySettingsRequest;
import com.inawulot.wallet.dto.UserResponse;
import com.inawulot.wallet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import com.inawulot.wallet.security.CurrentUser;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentUser currentUser;
    private final boolean allowSelfKycApproval;

    public UserController(UserService userService, CurrentUser currentUser,
                          @Value("${app.demo.allow-self-kyc-approval}") boolean allowSelfKycApproval) {
        this.userService = userService;
        this.currentUser = currentUser;
        this.allowSelfKycApproval = allowSelfKycApproval;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request));
    }

    @GetMapping
    public List<UserResponse> listUsers(Authentication authentication) {
        return List.of(UserResponse.from(userService.getUser(currentUser.id(authentication))));
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable UUID userId, Authentication authentication) {
        currentUser.require(userId, authentication);
        return UserResponse.from(userService.getUser(userId));
    }

    @PutMapping("/{userId}/profile")
    public UserResponse updateProfile(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        currentUser.require(userId, authentication);
        return UserResponse.from(userService.updateProfile(userId, request));
    }

    @PutMapping("/{userId}/profile-picture")
    public UserResponse updateProfilePicture(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileImageRequest request, Authentication authentication) {
        currentUser.require(userId, authentication);
        return UserResponse.from(userService.updateProfileImage(userId, request));
    }

    @PutMapping("/{userId}/security")
    public UserResponse updateSecuritySettings(@PathVariable UUID userId, @Valid @RequestBody UpdateSecuritySettingsRequest request, Authentication authentication) {
        currentUser.require(userId, authentication);
        return UserResponse.from(userService.updateSecuritySettings(userId, request));
    }

    @PostMapping("/{userId}/kyc")
    public UserResponse submitKyc(@PathVariable UUID userId, @Valid @RequestBody SubmitKycRequest request, Authentication authentication) {
        currentUser.require(userId, authentication);
        return UserResponse.from(userService.submitKyc(userId, request));
    }

    @PostMapping("/{userId}/kyc/approve")
    public UserResponse approveKyc(@PathVariable UUID userId, Authentication authentication) {
        currentUser.require(userId, authentication);
        if (!allowSelfKycApproval) {
            throw new com.inawulot.wallet.exception.ComplianceException("KYC approval is performed by a compliance reviewer");
        }
        return UserResponse.from(userService.approveKyc(userId));
    }
}
