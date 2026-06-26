package com.arul.finance_backend.ledger.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arul.finance_backend.ledger.dto.TransferRequest;
import com.arul.finance_backend.ledger.dto.TransferResponse;
import com.arul.finance_backend.ledger.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://payment-ledger-frontend.onrender.com/")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        try {
            var transfer = transferService.transfer(idempotencyKey, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TransferResponse.from(transfer));
        } catch (DataIntegrityViolationException race) {
            // Two identical requests raced past the fast-path check; the unique
            // constraint rejected the second insert. Return the winner's result
            // so the client still sees a single, consistent transfer.
            var existing = transferService.getByIdempotencyKey(idempotencyKey);
            return ResponseEntity.ok(TransferResponse.from(existing));
        }
    }

    @GetMapping("/{idempotencyKey}")
    public ResponseEntity<TransferResponse> getByKey(@PathVariable String idempotencyKey) {
        return ResponseEntity.ok(
                TransferResponse.from(transferService.getByIdempotencyKey(idempotencyKey)));
    }
}
