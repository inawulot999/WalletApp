package com.inawulot.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletMvpApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsUserApprovesKycFundsWalletAndSimulatesTransfer() throws Exception {
        String userJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Popoola Toluwani Faith",
                                  "email": "inawulot999@gmail.com",
                                  "phoneNumber": "+2349012208818",
                                  "country": "NG"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode user = objectMapper.readTree(userJson);
        String userId = user.get("id").asText();

        mockMvc.perform(put("/api/users/{userId}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Popoola Toluwani Faith",
                                  "email": "inawulot999@gmail.com",
                                  "phoneNumber": "+2349012208818",
                                  "country": "NG",
                                  "residentialAddress": "Ibadan, Oyo State"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Popoola Toluwani Faith"))
                .andExpect(jsonPath("$.email").value("inawulot999@gmail.com"));

        mockMvc.perform(put("/api/users/{userId}/profile-picture", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageUrl": "https://example.com/inawulot999.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/inawulot999.png"));

        mockMvc.perform(put("/api/users/{userId}/security", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "twoFactorAuthenticatorEnabled": true,
                                  "fingerprintEnabled": true,
                                  "pinLockEnabled": true,
                                  "immediateLockEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorAuthenticatorEnabled").value(true))
                .andExpect(jsonPath("$.fingerprintEnabled").value(true))
                .andExpect(jsonPath("$.pinLockEnabled").value(true))
                .andExpect(jsonPath("$.immediateLockEnabled").value(true));

        mockMvc.perform(post("/api/users/{userId}/kyc", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bvn": "22790575513",
                                  "nin": "52267738961",
                                  "residentialAddress": "Oyo State, Nigeria"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("SUBMITTED"));

        mockMvc.perform(post("/api/users/{userId}/kyc/approve", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("VERIFIED"));

        mockMvc.perform(post("/api/wallets/{userId}/fund", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "NGN",
                                  "amount": 100000,
                                  "memo": "Demo funding"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("NGN"))
                .andExpect(jsonPath("$.balance").value(100000.00));

        mockMvc.perform(post("/api/transfers/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceUserId": "%s",
                                  "sourceCurrency": "NGN",
                                  "targetCurrency": "USD",
                                  "sourceAmount": 25000,
                                  "transferType": "CROSS_BORDER",
                                  "recipientName": "Sample Recipient",
                                  "destinationCountry": "US",
                                  "destinationReference": "US-BANK-SANDBOX-001"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SIMULATED"))
                .andExpect(jsonPath("$.estimatedTargetAmount").value(14.70));

        mockMvc.perform(post("/api/transfers/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceCurrency": "NGN",
                                  "targetCurrency": "USDT",
                                  "sourceAmount": 25000,
                                  "transferType": "EXCHANGE_WALLET"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("USDT"))
                .andExpect(jsonPath("$.estimatedTargetAmount").value(14.70));

        mockMvc.perform(post("/api/transfers/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceUserId": "%s",
                                  "sourceCurrency": "NGN",
                                  "targetCurrency": "USDT",
                                  "sourceAmount": 25000,
                                  "transferType": "EXCHANGE_WALLET",
                                  "recipientName": "Bybit Wallet",
                                  "destinationCountry": "NG",
                                  "destinationReference": "WA2948750"
                                }
                                """.formatted(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Exchange wallet transfers are coming soon in Dior Wallet"));

        mockMvc.perform(get("/api/wallets/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(75000.00));
    }
}
