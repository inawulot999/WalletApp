package com.inawulot.wallet.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsDeliveryService {
    private final boolean enabled;
    private final String accountSid;
    private final String authToken;
    private final String from;
    private final HttpClient client = HttpClient.newHttpClient();

    public SmsDeliveryService(
            @Value("${app.sms.enabled}") boolean enabled,
            @Value("${app.sms.twilio-account-sid:}") String accountSid,
            @Value("${app.sms.twilio-auth-token:}") String authToken,
            @Value("${app.sms.from:}") String from
    ) {
        this.enabled = enabled;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.from = from;
    }

    public void sendLoginCode(String phoneNumber, String code) {
        if (!enabled || accountSid.isBlank() || authToken.isBlank() || from.isBlank()) {
            throw new IllegalStateException("SMS sign-in is not configured yet");
        }
        String body = form("To", phoneNumber) + "&" + form("From", from) + "&" + form(
                "Body", "Your WalletApp sign-in code is " + code + ". It expires in 10 minutes. Do not share this code."
        );
        String credentials = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("SMS delivery could not be completed");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS delivery was interrupted");
        } catch (Exception exception) {
            throw new IllegalStateException("SMS delivery could not be completed");
        }
    }

    private String form(String name, String value) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
