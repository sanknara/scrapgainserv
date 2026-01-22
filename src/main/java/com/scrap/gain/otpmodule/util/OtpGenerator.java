package com.scrap.gain.otpmodule.util;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpGenerator {

    private final OtpConfigProperties config;

    // SecureRandom is thread-safe and cryptographically strong
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generate a cryptographically secure random OTP
     * @return OTP string of configured length (default 6 digits)
     */
    public String generate() {
        int length = config.getLength();
        StringBuilder otp = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            otp.append(SECURE_RANDOM.nextInt(10)); // 0-9
        }

        String generatedOtp = otp.toString();
        log.debug("Generated OTP of length {}", length);
        return generatedOtp;
    }

    /**
     * Generate alphanumeric OTP (more secure, harder to brute force)
     * @return Alphanumeric OTP string
     */
    public String generateAlphanumeric() {
        int length = config.getLength();
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder otp = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            otp.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }

        return otp.toString();
    }
}
