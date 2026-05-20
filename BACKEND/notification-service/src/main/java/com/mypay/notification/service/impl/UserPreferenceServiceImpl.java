package com.mypay.notification.service.impl;

import com.mypay.notification.dto.UpdateUserPreferenceRequest;
import com.mypay.notification.dto.UserPreferenceResponse;
import com.mypay.notification.entity.UserPreference;
import com.mypay.notification.mapper.UserPreferenceMapper;
import com.mypay.notification.repository.UserPreferenceRepository;
import com.mypay.notification.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserPreferenceMapper preferenceMapper;

    @Override
    @Transactional
    public UserPreferenceResponse getOrCreate(String userId) {
        UserPreference pref = preferenceRepository.findByUserPreferenceUserId(userId)
                .orElseGet(() -> preferenceRepository.save(
                        UserPreference.builder().userPreferenceUserId(userId).build()));
        return preferenceMapper.toResponse(pref);
    }

    @Override
    @Transactional
    public UserPreferenceResponse update(String userId, UpdateUserPreferenceRequest request) {
        UserPreference pref = preferenceRepository.findByUserPreferenceUserId(userId)
                .orElseGet(() -> UserPreference.builder().userPreferenceUserId(userId).build());
        preferenceMapper.applyUpdate(pref, request);
        return preferenceMapper.toResponse(preferenceRepository.save(pref));
    }
}
