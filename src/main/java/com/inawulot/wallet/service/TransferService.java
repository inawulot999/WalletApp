package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.TransferRecord;
import com.inawulot.wallet.domain.TransferStatus;
import com.inawulot.wallet.domain.TransferType;
import com.inawulot.wallet.dto.QuoteRequest;
import com.inawulot.wallet.dto.QuoteResponse;
import com.inawulot.wallet.dto.TransferRequest;
import com.inawulot.wallet.exception.ComplianceException;
import com.inawulot.wallet.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransferService {
    private final UserService userService;
    private final WalletService walletService;
    private final QuoteService quoteService;
    private final Map<UUID, TransferRecord> transfers = new ConcurrentHashMap<>();

    public TransferService(UserService userService, WalletService walletService, QuoteService quoteService) {
        this.userService = userService;
        this.walletService = walletService;
        this.quoteService = quoteService;
    }

    public QuoteResponse quote(QuoteRequest request) {
        return quoteService.quote(request);
    }

    public TransferRecord simulateTransfer(TransferRequest request) {
        if (request.transferType() == TransferType.EXCHANGE_WALLET) {
            throw new ComplianceException("Exchange wallet transfers are coming soon in Dior Wallet");
        }

        userService.requireVerified(request.sourceUserId());
        QuoteResponse quote = quoteService.quote(new QuoteRequest(
                request.sourceCurrency(),
                request.targetCurrency(),
                request.sourceAmount(),
                request.transferType()
        ));

        UUID transferId = UUID.randomUUID();
        walletService.debitUserToSettlement(
                transferId,
                request.sourceUserId(),
                quote.sourceCurrency(),
                quote.sourceAmount(),
                "Simulated " + request.transferType() + " transfer to " + request.destinationCountry()
        );

        TransferRecord record = new TransferRecord(
                transferId,
                Instant.now(),
                request.sourceUserId(),
                request.transferType(),
                TransferStatus.SIMULATED,
                quote.sourceCurrency(),
                quote.targetCurrency(),
                quote.sourceAmount(),
                quote.feeAmount(),
                quote.exchangeRate(),
                quote.estimatedTargetAmount(),
                request.recipientName().trim(),
                request.destinationCountry().trim().toUpperCase(),
                request.destinationReference().trim(),
                "No real money, bank, crypto, or remittance rail was used"
        );
        transfers.put(transferId, record);
        return record;
    }

    public TransferRecord getTransfer(UUID transferId) {
        TransferRecord transfer = transfers.get(transferId);
        if (transfer == null) {
            throw new NotFoundException("Transfer not found");
        }
        return transfer;
    }
}
