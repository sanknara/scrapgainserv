package com.scrap.gain.otpmodule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponse {

    // Reference ID for tracking
    private String referenceId;

    // Masked identifier for display
    private String maskedIdentifier;

    // Expiry timestamp
    private Instant expiresAt;

    // Whether validation was successful
    private Boolean valid;

    // Remaining attempts (for failed validation)
    private Integer remainingAttempts;

    // Error code if any
    private String errorCode;

    // Human-readable message
    private String message;

    // Verification token (optional, for successful validation)
    private String verificationToken;

    // Factory methods for common responses
    public static OtpResponse success(String referenceId, String maskedId, Instant expiresAt) {
        return OtpResponse.builder()
                .referenceId(referenceId)
                .maskedIdentifier(maskedId)
                .expiresAt(expiresAt)
                .message("OTP sent successfully")
                .build();
    }

    public static OtpResponse validationSuccess(String token) {
        return OtpResponse.builder()
                .valid(true)
                .verificationToken(token)
                .message("OTP verified successfully")
                .build();
    }

    public static OtpResponse validationFailed(int remainingAttempts, String errorCode) {
        return OtpResponse.builder()
                .valid(false)
                .remainingAttempts(remainingAttempts)
                .errorCode(errorCode)
                .message("Invalid OTP")
                .build();
    }

    public static OtpResponse error(String errorCode, String message) {
        return OtpResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
