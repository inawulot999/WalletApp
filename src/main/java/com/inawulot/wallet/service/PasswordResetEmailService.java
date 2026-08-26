package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.WalletUser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String frontendBaseUrl;

    public PasswordResetEmailService(JavaMailSender mailSender,
                                     @Value("${app.mail.enabled}") boolean enabled,
                                     @Value("${app.mail.from}") String from,
                                     @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void send(WalletUser user, String token) {
        if (!enabled || from.isBlank()) {
            throw new IllegalStateException("Password reset email is not configured yet");
        }
        String resetUrl = frontendBaseUrl + "/?resetToken=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Reset your WalletApp password");
        message.setText("Hello " + user.getFullName() + ",\n\nUse this link to reset your WalletApp password. It expires in 15 minutes:\n" + resetUrl + "\n\nIf you did not request this, you can safely ignore this email.");
        mailSender.send(message);
    }
}
