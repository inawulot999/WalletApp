package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.CryptoNetwork;
import com.inawulot.wallet.domain.CryptoWalletAddress;
import com.inawulot.wallet.domain.TransferRecord;
import com.inawulot.wallet.domain.TransferStatus;
import com.inawulot.wallet.domain.TransferType;
import com.inawulot.wallet.dto.QuoteRequest;
import com.inawulot.wallet.dto.QuoteResponse;
import com.inawulot.wallet.dto.SendWalletRequest;
import com.inawulot.wallet.dto.TransferRequest;
import com.inawulot.wallet.exception.ComplianceException;
import com.inawulot.wallet.exception.NotFoundException;
import com.inawulot.wallet.repository.TransferRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {
    private final UserService userService;
    private final WalletService walletService;
    private final QuoteService quoteService;
    private final TransferRecordRepository transferRecordRepository;
    private final WalletAddressService walletAddressService;
    private final InputSanitizer inputSanitizer;
    private final HashingService hashingService;

    public TransferService(
            UserService userService,
            WalletService walletService,
            QuoteService quoteService,
            TransferRecordRepository transferRecordRepository,
            WalletAddressService walletAddressService,
            InputSanitizer inputSanitizer,
            HashingService hashingService
    ) {
        this.userService = userService;
        this.walletService = walletService;
        this.quoteService = quoteService;
        this.transferRecordRepository = transferRecordRepository;
        this.walletAddressService = walletAddressService;
        this.inputSanitizer = inputSanitizer;
        this.hashingService = hashingService;
    }

    public QuoteResponse quote(QuoteRequest request) {
        return quoteService.quote(request);
    }

    @Transactional
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
        return transferRecordRepository.save(record);
    }

    @Transactional
    public TransferRecord sendWalletTransfer(SendWalletRequest request) {
        String asset = inputSanitizer.currency(request.asset());
        if (!"USDT".equals(asset)) {
            throw new IllegalArgumentException("Only USDT transfers are supported in this release");
        }

        CryptoNetwork network = request.network();
        String recipientAddress = inputSanitizer.clean(request.recipientAddress(), 96);
        if (!network.isValidAddress(recipientAddress)) {
            throw new IllegalArgumentException("Invalid " + network + " address format");
        }

        userService.requireVerified(request.userId());
        userService.requireValidTransferVerification(request.userId(), request.verificationCode());

        BigDecimal amount = money(request.amount());
        BigDecimal fee = network.getEstimatedFee().setScale(8, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = amount.add(fee).setScale(8, RoundingMode.HALF_UP);
        BigDecimal currentBalance = walletService.getBalance(request.userId(), asset);
        if (currentBalance.compareTo(totalDeduction) < 0) {
            throw new ComplianceException("Insufficient USDT balance for amount plus network fee");
        }

        UUID transactionId = UUID.randomUUID();
        CryptoWalletAddress sender = walletAddressService.getOrCreateAddress(request.userId(), network);
        walletService.debitUserAsset(
                transactionId,
                request.userId(),
                asset,
                totalDeduction,
                "USDT " + network + " transfer hold"
        );

        TransferRecord record = TransferRecord.pendingOnChain(
                transactionId,
                Instant.now(),
                request.userId(),
                asset,
                network,
                amount,
                fee,
                sender.getAddress(),
                recipientAddress,
                inputSanitizer.clean(request.recipientLabel(), 160).isBlank() ? "External wallet" : inputSanitizer.clean(request.recipientLabel(), 160),
                inputSanitizer.clean(request.memo(), 512)
        );
        return transferRecordRepository.save(record);
    }

    public TransferRecord getTransfer(UUID transferId) {
        return transferRecordRepository.findById(transferId)
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
    }

    public Page<TransferRecord> getHistory(UUID userId, TransferStatus status, int page, int size) {
        userService.getUser(userId);
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        if (status == null) {
            return transferRecordRepository.findBySourceUserIdOrderByCreatedAtDesc(userId, pageRequest);
        }
        return transferRecordRepository.findBySourceUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageRequest);
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void monitorPendingTransfers() {
        Instant cutoff = Instant.now().minusSeconds(8);
        transferRecordRepository.findTop25ByStatusOrderByCreatedAtAsc(TransferStatus.PENDING).stream()
                .filter(record -> record.getCreatedAt().isBefore(cutoff))
                .forEach(record -> record.markCompleted(simulatedTxHash(record.getId())));
    }

    private String simulatedTxHash(UUID transactionId) {
        return "0x" + hashingService.sha256("tx:" + transactionId).substring(0, 64);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(8, RoundingMode.HALF_UP);
    }
}
