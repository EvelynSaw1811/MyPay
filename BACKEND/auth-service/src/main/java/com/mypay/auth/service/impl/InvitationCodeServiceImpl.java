package com.mypay.auth.service.impl;

import com.mypay.auth.repository.UserRepository;
import com.mypay.auth.service.InvitationCodeService;
import com.mypay.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class InvitationCodeServiceImpl implements InvitationCodeService {

    private static final String PREFIX = "MP-";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 20;

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateUniqueInvitationCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = PREFIX + randomSuffix();
            if (!userRepository.existsByUserInvitationCode(code)) {
                return code;
            }
        }
        throw new ConflictException("Could not generate a unique invitation code");
    }

    private String randomSuffix() {
        StringBuilder suffix = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            suffix.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return suffix.toString();
    }
}
