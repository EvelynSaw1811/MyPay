package com.mypay.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public final class NotificationHelper {

    private NotificationHelper() {}

    public static <T> void fireAndForget(Supplier<T> notificationCall) {
        try {
            notificationCall.get();
        } catch (Exception e) {
            log.warn("Notification delivery failed (non-critical): {}", e.getMessage());
        }
    }

    public static void fireAndForget(Runnable notificationCall) {
        try {
            notificationCall.run();
        } catch (Exception e) {
            log.warn("Notification delivery failed (non-critical): {}", e.getMessage());
        }
    }
}
