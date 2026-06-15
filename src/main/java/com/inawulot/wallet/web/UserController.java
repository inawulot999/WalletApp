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

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request));
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable UUID userId) {
        return UserResponse.from(userService.getUser(userId));
    }

    @PutMapping("/{userId}/profile")
    public UserResponse updateProfile(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request) {
        return UserResponse.from(userService.updateProfile(userId, request));
    }

    @PutMapping("/{userId}/profile-picture")
    public UserResponse updateProfilePicture(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileImageRequest request) {
        return UserResponse.from(userService.updateProfileImage(userId, request));
    }

    @PutMapping("/{userId}/security")
    public UserResponse updateSecuritySettings(@PathVariable UUID userId, @Valid @RequestBody UpdateSecuritySettingsRequest request) {
        return UserResponse.from(userService.updateSecuritySettings(userId, request));
    }

    @PostMapping("/{userId}/kyc")
    public UserResponse submitKyc(@PathVariable UUID userId, @Valid @RequestBody SubmitKycRequest request) {
        return UserResponse.from(userService.submitKyc(userId, request));
    }

    @PostMapping("/{userId}/kyc/approve")
    public UserResponse approveKyc(@PathVariable UUID userId) {
        return UserResponse.from(userService.approveKyc(userId));
    }
}
