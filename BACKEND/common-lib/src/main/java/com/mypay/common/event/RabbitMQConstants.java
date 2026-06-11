package com.mypay.common.event;

public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    // Notification exchange (collection + transaction → notification-service)
    public static final String EXCHANGE = "mypay.notifications";

    public static final String QUEUE_SETTLEMENT  = "mypay.notifications.settlement";
    public static final String QUEUE_EXPENSE     = "mypay.notifications.expense";
    public static final String QUEUE_INVITATION  = "mypay.notifications.invitation";

    public static final String KEY_SETTLEMENT = "notification.settlement.#";
    public static final String KEY_EXPENSE    = "notification.expense.#";
    public static final String KEY_INVITATION = "notification.invitation.#";

    public static final String ROUTING_SETTLEMENT_RECEIVED  = "notification.settlement.received";
    public static final String ROUTING_SETTLEMENT_CONFIRMED = "notification.settlement.confirmed";
    public static final String ROUTING_SETTLEMENT_REMINDER  = "notification.settlement.reminder";
    public static final String ROUTING_EXPENSE_CREATED      = "notification.expense.created";
    public static final String ROUTING_INVITATION_RECEIVED  = "notification.invitation.received";

    // User lifecycle exchange (auth-service → wallet-service)
    public static final String USER_EXCHANGE       = "mypay.users";
    public static final String QUEUE_USER_WALLET   = "mypay.users.wallet";
    public static final String QUEUE_USER_COLLECTION = "mypay.users.collection";
    public static final String KEY_USER_REGISTERED = "user.registered";
}
