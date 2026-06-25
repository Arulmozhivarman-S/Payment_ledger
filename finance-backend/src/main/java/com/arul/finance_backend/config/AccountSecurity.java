package com.arul.finance_backend.config;

import org.springframework.stereotype.Component;

import com.arul.finance_backend.ledger.model.Account;
import com.arul.finance_backend.ledger.model.User;
import com.arul.finance_backend.ledger.repository.AccountRepository;
import com.arul.finance_backend.ledger.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component("accountSecurity")
@RequiredArgsConstructor
public class AccountSecurity {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public boolean canActAs(Long userId, String UserName){
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return false;
        return user.getUserName().equals(UserName);
    }

    public boolean isOwner(Long id, String userName){
        
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null) return false;
        Long ownerId = account.getOwnerUserId();
        User user = userRepository.findById(ownerId).orElseThrow();

        return user.getUserName().equals(userName);
    }


}
