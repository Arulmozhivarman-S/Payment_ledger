package com.arul.finance_backend.ledger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arul.finance_backend.ledger.dto.AccountResponse;
import com.arul.finance_backend.ledger.service.AccountService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    public record OpenAccountRequest(
            @NotNull Long ownerUserId,
            @NotNull @Size(min = 3, max = 3) String currency,
            long openingBalanceMinor) {}


    @PreAuthorize( " hasRole('ADMIN') or @accountSecurity.canActAs(#req.ownerUserId, authentication.name) " )
    @PostMapping
    public ResponseEntity<AccountResponse> open(@RequestBody OpenAccountRequest req) {
        var account = accountService.open(
                req.ownerUserId(), req.currency(), req.openingBalanceMinor());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    
    @PreAuthorize(  "  hasRole('ADMIN') or @accountSecurity.isOwner(#id, authentication.name ) " )
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.get(id)));
    }
}
