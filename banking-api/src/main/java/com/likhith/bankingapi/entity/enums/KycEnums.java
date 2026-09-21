package com.likhith.bankingapi.entity.enums;

public final class KycEnums {

    private KycEnums() {
    }

    public enum VerificationType {
        AADHAAR, PAN, PASSPORT, VIDEO_KYC, BIOMETRIC
    }

    public enum VerificationChannel {
        ONLINE, BRANCH, VIDEO_CALL
    }

    public enum KycVerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}
