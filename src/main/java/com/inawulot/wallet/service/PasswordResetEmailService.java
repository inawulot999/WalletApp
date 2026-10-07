package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.WalletUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public PasswordResetEmailService(JavaMailSender mailSender,
                                     @Value("${app.mail.enabled}") boolean enabled,
                                     @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(WalletUser user, String token) {
        if (!enabled || from.isBlank()) {
            throw new IllegalStateException("Password reset email is not configured yet");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Reset your WalletApp password");
        message.setText("Hello " + user.getFullName() + ",\n\nYour WalletApp password reset code is:\n\n" + token + "\n\nThis code expires in 15 minutes. Do not share it with anyone. If you did not request this, you can safely ignore this email.");
        mailSender.send(message);
    }
}
