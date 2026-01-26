package com.scrap.gain.otpmodule.unit;

import com.scrap.gain.otpmodule.util.OtpHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OTP Hasher Tests")
class OtpHasherTest {

    private OtpHasher otpHasher;

    @BeforeEach
    void setUp() {
        otpHasher = new OtpHasher();
    }

    @Test
    @DisplayName("Should hash OTP with BCrypt")
    void shouldHashOtpWithBcrypt() {
        String otp = "123456";

        String hashedOtp = otpHasher.hashWithBcrypt(otp);

        assertThat(hashedOtp).isNotNull();
        assertThat(hashedOtp).isNotEqualTo(otp);
        assertThat(hashedOtp).startsWith("$2a$"); // BCrypt prefix
    }

    @Test
    @DisplayName("Should verify correct OTP with BCrypt")
    void shouldVerifyCorrectOtpWithBcrypt() {
        String otp = "123456";
        String hashedOtp = otpHasher.hashWithBcrypt(otp);

        boolean isValid = otpHasher.verifyBcrypt(otp, hashedOtp);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect OTP with BCrypt")
    void shouldRejectIncorrectOtpWithBcrypt() {
        String otp = "123456";
        String wrongOtp = "654321";
        String hashedOtp = otpHasher.hashWithBcrypt(otp);

        boolean isValid = otpHasher.verifyBcrypt(wrongOtp, hashedOtp);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should hash OTP with SHA-256")
    void shouldHashOtpWithSha256() {
        String otp = "123456";
        String salt = otpHasher.generateSalt();

        String hashedOtp = otpHasher.hashWithSha256(otp, salt);

        assertThat(hashedOtp).isNotNull();
        assertThat(hashedOtp).isNotEqualTo(otp);
    }

    @Test
    @DisplayName("Should verify correct OTP with SHA-256")
    void shouldVerifyCorrectOtpWithSha256() {
        String otp = "123456";
        String salt = otpHasher.generateSalt();
        String hashedOtp = otpHasher.hashWithSha256(otp, salt);

        boolean isValid = otpHasher.verifySha256(otp, hashedOtp, salt);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should generate unique salts")
    void shouldGenerateUniqueSalts() {
        String salt1 = otpHasher.generateSalt();
        String salt2 = otpHasher.generateSalt();

        assertThat(salt1).isNotEqualTo(salt2);
    }
}
