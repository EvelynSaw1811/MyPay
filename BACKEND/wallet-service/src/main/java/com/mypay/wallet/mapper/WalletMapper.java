package com.mypay.wallet.mapper;

import com.mypay.wallet.dto.AccountResponse;
import com.mypay.wallet.dto.PayeeResponse;
import com.mypay.wallet.dto.WalletResponse;
import com.mypay.wallet.entity.Account;
import com.mypay.wallet.entity.Payee;
import com.mypay.wallet.entity.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletMapper {

    public AccountResponse toAccountResponse(Account account, List<Wallet> wallets) {
        List<WalletResponse> walletResponses = wallets.stream().map(this::toWalletResponse).toList();
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .userId(account.getAccountUserId())
                .wallets(walletResponses)
                .accounts(walletResponses)
                .createdAt(account.getAccountCreated())
                .updatedAt(account.getAccountUpdated())
                .build();
    }

    public WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .accountId(wallet.getAccount().getAccountId())
                .currency(wallet.getWalletCurrency())
                .balance(wallet.getWalletBalance())
                .status(wallet.getWalletStatus())
                .createdAt(wallet.getWalletCreated())
                .updatedAt(wallet.getWalletUpdated())
                .build();
    }

    public PayeeResponse toPayeeResponse(Payee payee) {
        return PayeeResponse.builder()
                .payeeId(payee.getPayeeId())
                .userId(payee.getPayeeUserId())
                .nickname(payee.getPayeeNickname())
                .createdAt(payee.getPayeeCreated())
                .build();
    }
}
