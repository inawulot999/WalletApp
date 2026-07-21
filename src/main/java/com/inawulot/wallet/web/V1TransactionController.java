package com.inawulot.wallet.web;

import com.inawulot.wallet.domain.TransferStatus;
import com.inawulot.wallet.dto.TransactionHistoryResponse;
import com.inawulot.wallet.dto.TransferResponse;
import com.inawulot.wallet.service.TransferService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class V1TransactionController {
    private final TransferService transferService;

    public V1TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/history")
    public TransactionHistoryResponse history(
            @RequestParam UUID userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TransferStatus parsedStatus = parseStatus(status);
        var result = transferService.getHistory(userId, parsedStatus, page, size);
        return new TransactionHistoryResponse(
                result.getContent().stream().map(TransferResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private TransferStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TransferStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported transaction status filter");
        }
    }
}
