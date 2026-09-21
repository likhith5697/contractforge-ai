package com.likhith.bankingapi.entity.enums;

/**
 * Enumerations shared across multiple domains.
 */
public final class CommonEnums {

    private CommonEnums() {
    }

    public enum Channel {
        WEB, MOBILE, BRANCH, ATM, API, IVR
    }

    public enum RelationshipType {
        SPOUSE, PARENT, CHILD, SIBLING, FRIEND, BUSINESS_PARTNER, OTHER
    }
}
