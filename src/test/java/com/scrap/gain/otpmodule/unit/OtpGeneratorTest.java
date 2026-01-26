package com.scrap.gain.otpmodule.unit;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import com.scrap.gain.otpmodule.util.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTP Generator Tests")
class OtpGeneratorTest {

    @Mock
    private OtpConfigProperties config;

    private OtpGenerator otpGenerator;

    @BeforeEach
    void setUp() {
        when(config.getLength()).thenReturn(6);
        otpGenerator = new OtpGenerator(config);
    }

    @Test
    @DisplayName("Should generate OTP of configured length")
    void shouldGenerateOtpOfConfiguredLength() {
        String otp = otpGenerator.generate();

        assertThat(otp).hasSize(6);
    }

    @Test
    @DisplayName("Should generate numeric only OTP")
    void shouldGenerateNumericOnlyOtp() {
        String otp = otpGenerator.generate();

        assertThat(otp).matches("\\d{6}");
    }

    @Test
    @DisplayName("Should generate unique OTPs")
    void shouldGenerateUniqueOtps() {
        Set<String> otps = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            otps.add(otpGenerator.generate());
        }

        // At least 95% should be unique (very high probability)
        assertThat(otps.size()).isGreaterThan(90);
    }

    @Test
    @DisplayName("Should generate alphanumeric OTP when requested")
    void shouldGenerateAlphanumericOtp() {
        String otp = otpGenerator.generateAlphanumeric();

        assertThat(otp).hasSize(6);
        assertThat(otp).matches("[0-9A-Z]{6}");
    }
}
