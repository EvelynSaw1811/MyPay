package com.mypay.wallet.service.impl;

import com.mypay.common.constant.CurrencyCode;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ConflictException;
import com.mypay.common.exception.DuplicateResourceException;
import com.mypay.common.exception.InsufficientBalanceException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.common.util.MoneyUtil;
import com.mypay.wallet.dto.AccountResponse;
import com.mypay.wallet.dto.OpenWalletRequest;
import com.mypay.wallet.dto.TopUpRequest;
import com.mypay.wallet.dto.WalletOperationRequest;
import com.mypay.wallet.dto.WalletResponse;
import com.mypay.wallet.dto.WalletRegistrationStatusResponse;
import com.mypay.wallet.entity.Account;
import com.mypay.wallet.entity.Wallet;
import com.mypay.wallet.mapper.WalletMapper;
import com.mypay.wallet.repository.AccountRepository;
import com.mypay.wallet.repository.WalletRepository;
import com.mypay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final int MAX_WALLETS_PER_ACCOUNT = 3;

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(String userId, Set<String> walletCurrencies) {
        if (accountRepository.existsByAccountUserId(userId)) {
            throw new DuplicateResourceException("Account already exists for user: " + userId);
        }

        Account account = accountRepository.save(Account.builder().accountUserId(userId).build());
        List<Wallet> wallets = normalizeCurrencies(walletCurrencies).stream()
                .map(currency -> Wallet.builder()
                        .account(account)
                        .walletUserId(userId)
                        .walletCurrency(currency)
                        .walletBalance(BigDecimal.ZERO)
                        .walletStatus("ACTIVE")
                        .build())
                .toList();
        walletRepository.saveAll(wallets);

        return walletMapper.toAccountResponse(account, wallets);
    }

    @Override
    @Transactional
    public AccountResponse createWallet(String userId) {
        return createAccount(userId, Set.of(CurrencyCode.DEFAULT_CODE));
    }

    @Override
    public AccountResponse getMyAccount(String userId) {
        Account account = findAccountByUserId(userId);
        List<Wallet> wallets = walletRepository.findByAccount_AccountId(account.getAccountId());
        return walletMapper.toAccountResponse(account, wallets);
    }

    @Override
    public AccountResponse getMyWallet(String userId) {
        return getMyAccount(userId);
    }

    @Override
    @Transactional
    public WalletResponse openWallet(String userId, OpenWalletRequest request) {
        Account account = findAccountByUserId(userId);
        String currency = normalizeCurrency(request.getCurrency());

        if (walletRepository.existsByAccount_AccountIdAndWalletCurrency(account.getAccountId(), currency)) {
            throw new DuplicateResourceException(currency + " wallet already exists");
        }
        if (walletRepository.countByAccount_AccountId(account.getAccountId()) >= MAX_WALLETS_PER_ACCOUNT) {
            throw new ConflictException("Account already has the maximum number of wallets");
        }

        Wallet wallet = Wallet.builder()
                .account(account)
                .walletUserId(userId)
                .walletCurrency(currency)
                .walletBalance(BigDecimal.ZERO)
                .walletStatus("ACTIVE")
                .build();
        return walletMapper.toWalletResponse(walletRepository.save(wallet));
    }

    @Override
    public WalletRegistrationStatusResponse getRegistrationStatus(String userId, String currency) {
        Account account = findAccountByUserId(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        boolean walletExists = walletRepository.existsByAccount_AccountIdAndWalletCurrency(
                account.getAccountId(),
                normalizedCurrency);
        boolean canRegister = !walletExists
                && walletRepository.countByAccount_AccountId(account.getAccountId()) < MAX_WALLETS_PER_ACCOUNT;
        return WalletRegistrationStatusResponse.builder()
                .currency(normalizedCurrency)
                .walletExists(walletExists)
                .canRegister(canRegister)
                .build();
    }

    @Override
    public WalletResponse getBalance(String userId, String currency) {
        Wallet wallet = findWallet(userId, currency);
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse topUp(String userId, TopUpRequest request) {
        Wallet wallet = findWallet(userId, request.getCurrency());
        wallet.setWalletBalance(MoneyUtil.add(wallet.getWalletBalance(), request.getAmount()));
        return walletMapper.toWalletResponse(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public void debit(String userId, WalletOperationRequest request) {
        Wallet wallet = findWallet(userId, request.getCurrency());
        if (wallet.getWalletBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient " + request.getCurrency() + " balance for user: " + userId);
        }
        wallet.setWalletBalance(MoneyUtil.subtract(wallet.getWalletBalance(), request.getAmount()));
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void credit(String userId, WalletOperationRequest request) {
        Wallet wallet = findWallet(userId, request.getCurrency());
        wallet.setWalletBalance(MoneyUtil.add(wallet.getWalletBalance(), request.getAmount()));
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public WalletResponse closeWallet(String userId, String currency) {
        String normalizedCurrency = normalizeCurrency(currency);
        Wallet wallet = findWallet(userId, normalizedCurrency);

        if ("CLOSED".equalsIgnoreCase(wallet.getWalletStatus())) {
            throw new ConflictException(normalizedCurrency + " wallet is already closed");
        }
        if (wallet.getWalletBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflictException(
                    "Cannot close wallet with a non-zero balance. Please withdraw or transfer the remaining funds first.");
        }

        wallet.setWalletStatus("CLOSED");
        return walletMapper.toWalletResponse(walletRepository.save(wallet));
    }

    private Account findAccountByUserId(String userId) {
        return accountRepository.findByAccountUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
    }

    private Wallet findWallet(String userId, String currency) {
        String normalizedCurrency = normalizeCurrency(currency);
        return walletRepository.findByAccount_AccountUserIdAndWalletCurrency(userId, normalizedCurrency)
                .orElseThrow(() -> new ResourceNotFoundException(
                        normalizedCurrency + " wallet not found for user: " + userId));
    }

    private Set<String> normalizeCurrencies(Set<String> requestedCurrencies) {
        Set<String> currencies = new LinkedHashSet<>();
        currencies.add(CurrencyCode.DEFAULT_CODE);
        if (requestedCurrencies != null) {
            requestedCurrencies.forEach(currency -> currencies.add(normalizeCurrency(currency)));
        }
        if (currencies.size() > MAX_WALLETS_PER_ACCOUNT) {
            throw new ConflictException("Account can have at most " + MAX_WALLETS_PER_ACCOUNT + " wallets");
        }
        return currencies;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new BadRequestException("Currency is required");
        }
        String normalized = currency.trim().toUpperCase();
        for (CurrencyCode code : CurrencyCode.values()) {
            if (code.getCode().equals(normalized)) {
                return normalized;
            }
        }
        throw new BadRequestException("Unsupported wallet currency: " + currency);
    }
}
