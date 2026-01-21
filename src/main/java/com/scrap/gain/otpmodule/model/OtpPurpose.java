package com.scrap.gain.otpmodule.model;

public enum OtpPurpose {
    LOGIN("Login verification"),
    REGISTRATION("New user registration"),
    PASSWORD_RESET("Password reset request"),
    TRANSACTION("Transaction verification"),
    TWO_FACTOR("Two-factor authentication"),
    PHONE_VERIFICATION("Phone number verification");

    private final String description;

    OtpPurpose(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
