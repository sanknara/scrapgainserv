package com.scrap.gain.otpmodule.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // OTP Generation Errors
    OTP_GENERATION_FAILED("OTP_001", "Failed to generate OTP"),
    OTP_ALREADY_SENT("OTP_002", "OTP already sent. Please wait before requesting again"),

    // OTP Validation Errors
    OTP_INVALID("OTP_010", "Invalid OTP"),
    OTP_EXPIRED("OTP_011", "OTP has expired"),
    OTP_MAX_ATTEMPTS("OTP_012", "Maximum validation attempts exceeded"),
    OTP_NOT_FOUND("OTP_013", "No OTP found for this identifier"),
    OTP_ALREADY_VERIFIED("OTP_014", "OTP already verified"),

    // Rate Limiting Errors
    RATE_LIMIT_EXCEEDED("OTP_020", "Too many requests. Please try again later"),

    // SMS Delivery Errors
    SMS_DELIVERY_FAILED("OTP_030", "Failed to send SMS"),
    SMS_PROVIDER_ERROR("OTP_031", "SMS provider error"),

    // Validation Errors
    INVALID_PHONE_FORMAT("OTP_040", "Invalid phone number format"),
    INVALID_EMAIL_FORMAT("OTP_041", "Invalid email format"),
    INVALID_REQUEST("OTP_042", "Invalid request"),

    // System Errors
    INTERNAL_ERROR("OTP_500", "Internal server error");

    private final String code;
    private final String message;
}
