package com.scrap.gain.otpmodule.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class OtpHasher {

    // BCrypt for secure hashing (slower but more secure)
    private final PasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(10);

    /**
     * Hash OTP using BCrypt (recommended for high security)
     * Slower but resistant to rainbow table attacks
     */
    public String hashWithBcrypt(String otp) {
        return bcryptEncoder.encode(otp);
    }

    /**
     * Verify OTP against BCrypt hash
     */
    public boolean verifyBcrypt(String plainOtp, String hashedOtp) {
        return bcryptEncoder.matches(plainOtp, hashedOtp);
    }

    /**
     * Hash OTP using SHA-256 with salt (faster, acceptable for short-lived OTPs)
     * Use this if performance is critical and OTP expiry is short (< 5 min)
     */
    public String hashWithSha256(String otp, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedOtp = otp + salt;
            byte[] hash = digest.digest(saltedOtp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Verify OTP against SHA-256 hash
     */
    public boolean verifySha256(String plainOtp, String hashedOtp, String salt) {
        String computedHash = hashWithSha256(plainOtp, salt);
        return computedHash.equals(hashedOtp);
    }

    /**
     * Generate a random salt for SHA-256 hashing
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
