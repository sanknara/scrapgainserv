package com.scrap.gain.otpmodule.entity;

import com.scrap.gain.otpmodule.model.OtpPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique identifier for this OTP record
    private String id;

    // User identifier (phone number or email)
    private String identifier;

    // Hashed OTP - NEVER store plain text!
    private String otpHash;

    // Purpose of the OTP
    private OtpPurpose purpose;

    // Number of validation attempts made
    private int attemptCount;

    // Maximum allowed attempts
    private int maxAttempts;

    // Creation timestamp
    private Instant createdAt;

    // Expiry timestamp
    private Instant expiresAt;

    // Whether OTP has been verified
    private boolean verified;

    // Optional metadata (userId, sessionId, etc.)
    private Map<String, String> metadata;

    // Generate unique ID
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    // Check if OTP is expired
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // Check if max attempts exceeded
    public boolean isMaxAttemptsExceeded() {
        return attemptCount >= maxAttempts;
    }

    // Increment attempt count
    public void incrementAttempts() {
        this.attemptCount++;
    }

    // Generate Redis key
    public String getRedisKey() {
        return String.format("otp:%s:%s", identifier, purpose.name());
    }

    // Static method to generate Redis key
    public static String buildRedisKey(String identifier, OtpPurpose purpose) {
        return String.format("otp:%s:%s", identifier, purpose.name());
    }
}
