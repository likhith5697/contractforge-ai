package com.likhith.bankingapi.entity.enums;

public final class AccountEnums {

    private AccountEnums() {
    }

    public enum AccountType {
        SAVINGS, CURRENT, FIXED_DEPOSIT, SALARY
    }

    public enum AccountStatus {
        ACTIVE, DORMANT, CLOSED, FROZEN
    }

    public enum AccountPurpose {
        SALARY, SAVINGS_GOAL, BUSINESS, PERSONAL
    }
}
