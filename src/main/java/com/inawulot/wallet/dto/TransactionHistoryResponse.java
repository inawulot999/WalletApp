package com.inawulot.wallet.dto;

import java.util.List;

public record TransactionHistoryResponse(
        List<TransferResponse> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
