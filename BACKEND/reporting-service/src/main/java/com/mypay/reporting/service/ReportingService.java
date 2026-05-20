package com.mypay.reporting.service;

import com.mypay.reporting.dto.*;

public interface ReportingService {
    UserDashboardReport getDashboard(String userId);
    SpendingSummaryReport getSpendingSummary(String userId);
    CurrencyLedgerReport getCurrencyLedger(String userId, String currency);
    CollectionStatementReport getCollectionStatement(String userId, String collectionId);
    PersonalPositionReport getPersonalPosition(String userId);
}
