package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.QuoteRequest;
import com.inawulot.wallet.dto.QuoteResponse;
import com.inawulot.wallet.dto.TransferRequest;
import com.inawulot.wallet.dto.TransferResponse;
import com.inawulot.wallet.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import com.inawulot.wallet.security.CurrentUser;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;
    private final CurrentUser currentUser;

    public TransferController(TransferService transferService, CurrentUser currentUser) {
        this.transferService = transferService;
        this.currentUser = currentUser;
    }

    @PostMapping("/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return transferService.quote(request);
    }

    @PostMapping("/simulate")
    public TransferResponse simulate(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        currentUser.require(request.sourceUserId(), authentication);
        return TransferResponse.from(transferService.simulateTransfer(request));
    }

    @GetMapping("/{transferId}")
    public TransferResponse getTransfer(@PathVariable UUID transferId, Authentication authentication) {
        var transfer = transferService.getTransfer(transferId);
        currentUser.require(transfer.getSourceUserId(), authentication);
        return TransferResponse.from(transfer);
    }
}
