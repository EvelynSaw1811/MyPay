package com.mypay.wallet.service.impl;

import com.mypay.common.exception.DuplicateResourceException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.wallet.client.AuthClient;
import com.mypay.wallet.dto.PayeeRequest;
import com.mypay.wallet.dto.PayeeResponse;
import com.mypay.wallet.entity.Account;
import com.mypay.wallet.entity.Payee;
import com.mypay.wallet.mapper.WalletMapper;
import com.mypay.wallet.repository.AccountRepository;
import com.mypay.wallet.repository.PayeeRepository;
import com.mypay.wallet.service.PayeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PayeeServiceImpl implements PayeeService {

    private final PayeeRepository payeeRepository;
    private final AccountRepository accountRepository;
    private final WalletMapper walletMapper;
    private final AuthClient authClient;

    @Override
    public List<PayeeResponse> getPayees(String userId) {
        Account account = findAccount(userId);
        return payeeRepository.findByPayeeAccountId(account.getAccountId()).stream()
                .map(walletMapper::toPayeeResponse)
                .toList();
    }

    @Override
    @Transactional
    public PayeeResponse addPayee(String userId, PayeeRequest request) {
        Account account = findAccount(userId);
        String payeeUserId = Optional.ofNullable(request.getPayeeUserId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> resolveUserId(request.getIdentifier()));

        if (payeeUserId == null || payeeUserId.isBlank()) {
            throw new ResourceNotFoundException("Payee user ID is required");
        }

        if (payeeRepository.existsByPayeeAccountIdAndPayeeUserId(account.getAccountId(), payeeUserId)) {
            throw new DuplicateResourceException("Payee already added");
        }

        Payee payee = Payee.builder()
                .payeeAccountId(account.getAccountId())
                .payeeUserId(payeeUserId)
                .payeeNickname(Optional.ofNullable(request.getNickname()).filter(n -> !n.isBlank()).orElse(payeeUserId))
                .build();

        return walletMapper.toPayeeResponse(payeeRepository.save(payee));
    }

    @Override
    @Transactional
    public void removePayee(String userId, String payeeId) {
        Account account = findAccount(userId);
        Payee payee = payeeRepository.findByPayeeIdAndPayeeAccountId(payeeId, account.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found: " + payeeId));
        payeeRepository.delete(payee);
    }

    private Account findAccount(String userId) {
        return accountRepository.findByAccountUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
    }

    private String resolveUserId(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        var response = authClient.resolveUser(identifier.trim());
        if (response == null || response.getData() == null || response.getData().get("userId") == null) {
            throw new ResourceNotFoundException("User not found: " + identifier);
        }
        return response.getData().get("userId").toString();
    }
}
