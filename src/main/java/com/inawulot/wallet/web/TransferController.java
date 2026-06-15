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

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return transferService.quote(request);
    }

    @PostMapping("/simulate")
    public TransferResponse simulate(@Valid @RequestBody TransferRequest request) {
        return TransferResponse.from(transferService.simulateTransfer(request));
    }

    @GetMapping("/{transferId}")
    public TransferResponse getTransfer(@PathVariable UUID transferId) {
        return TransferResponse.from(transferService.getTransfer(transferId));
    }
}
