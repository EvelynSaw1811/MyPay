package com.mypay.wallet.service;

import com.mypay.wallet.dto.AccountResponse;
import com.mypay.wallet.dto.OpenWalletRequest;
import com.mypay.wallet.dto.TopUpRequest;
import com.mypay.wallet.dto.WalletOperationRequest;
import com.mypay.wallet.dto.WalletRegistrationStatusResponse;
import com.mypay.wallet.dto.WalletResponse;

import java.util.Set;

public interface WalletService {
    AccountResponse createAccount(String userId, Set<String> walletCurrencies);
    AccountResponse createWallet(String userId);
    AccountResponse getMyAccount(String userId);
    AccountResponse getMyWallet(String userId);
    WalletResponse openWallet(String userId, OpenWalletRequest request);
    WalletRegistrationStatusResponse getRegistrationStatus(String userId, String currency);
    WalletResponse getBalance(String userId, String currency);
    WalletResponse topUp(String userId, TopUpRequest request);
    void debit(String userId, WalletOperationRequest request);
    void credit(String userId, WalletOperationRequest request);
    WalletResponse closeWallet(String userId, String currency);
}
