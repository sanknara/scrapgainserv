package com.scrap.gain.otpmodule.unit;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import com.scrap.gain.otpmodule.dto.OtpGenerateRequest;
import com.scrap.gain.otpmodule.dto.OtpResponse;
import com.scrap.gain.otpmodule.dto.OtpValidateRequest;
import com.scrap.gain.otpmodule.entity.OtpRecord;
import com.scrap.gain.otpmodule.exception.ErrorCode;
import com.scrap.gain.otpmodule.exception.OtpException;
import com.scrap.gain.otpmodule.model.OtpPurpose;
import com.scrap.gain.otpmodule.repository.OtpRepository;
import com.scrap.gain.otpmodule.service.SmsService;
import com.scrap.gain.otpmodule.service.impl.OtpServiceImpl;
import com.scrap.gain.otpmodule.util.MaskingUtil;
import com.scrap.gain.otpmodule.util.OtpGenerator;
import com.scrap.gain.otpmodule.util.OtpHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTP Service Tests")
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;
    @Mock private OtpGenerator otpGenerator;
    @Mock private OtpHasher otpHasher;
    @Mock private MaskingUtil maskingUtil;
    @Mock (strictness = Mock.Strictness.LENIENT) private OtpConfigProperties config;
    @Mock private SmsService smsService;

    private OtpServiceImpl otpService;

    private static final String PHONE = "+919876543210";
    private static final String OTP = "123456";
    private static final String HASHED_OTP = "$2a$10$hashedOtp";

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(
                otpRepository, otpGenerator, otpHasher,
                maskingUtil, config, smsService
        );

        when(config.getMaxAttempts()).thenReturn(5);
        when(config.getExpiryMinutes()).thenReturn(5);
        when(maskingUtil.maskIdentifier(anyString())).thenReturn("+91****3210");
    }

    // ============ Generate OTP Tests ============

    @Test
    @DisplayName("Should generate OTP successfully")
    void shouldGenerateOtpSuccessfully() {
        // Given
        OtpGenerateRequest request = OtpGenerateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.empty());
        when(otpGenerator.generate()).thenReturn(OTP);
        when(otpHasher.hashWithBcrypt(OTP)).thenReturn(HASHED_OTP);
        when(otpRepository.save(any(OtpRecord.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(smsService).sendOtp(anyString(), anyString());

        // When
        OtpResponse response = otpService.generateOtp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReferenceId()).isNotNull();
        assertThat(response.getMessage()).isEqualTo("OTP sent successfully");

        verify(otpRepository).save(any(OtpRecord.class));
        verify(smsService).sendOtp(PHONE, OTP);
    }

    @Test
    @DisplayName("Should throw exception when OTP already sent")
    void shouldThrowExceptionWhenOtpAlreadySent() {
        // Given
        OtpGenerateRequest request = OtpGenerateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .build();

        OtpRecord existingRecord = OtpRecord.builder()
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES)) // Not expired
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.of(existingRecord));

        // When & Then
        assertThatThrownBy(() -> otpService.generateOtp(request))
                .isInstanceOf(OtpException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OTP_ALREADY_SENT);
    }

    // ============ Validate OTP Tests ============

    @Test
    @DisplayName("Should validate OTP successfully")
    void shouldValidateOtpSuccessfully() {
        // Given
        OtpValidateRequest request = OtpValidateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otp(OTP)
                .build();

        OtpRecord record = OtpRecord.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otpHash(HASHED_OTP)
                .attemptCount(0)
                .maxAttempts(5)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .verified(false)
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.of(record));
        when(otpHasher.verifyBcrypt(OTP, HASHED_OTP)).thenReturn(true);

        // When
        OtpResponse response = otpService.validateOtp(request);

        // Then
        assertThat(response.getValid()).isTrue();
        assertThat(response.getVerificationToken()).isNotNull();

        verify(otpRepository).delete(PHONE, OtpPurpose.LOGIN);
    }

    @Test
    @DisplayName("Should reject invalid OTP")
    void shouldRejectInvalidOtp() {
        // Given
        OtpValidateRequest request = OtpValidateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otp("000000")
                .build();

        OtpRecord record = OtpRecord.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otpHash(HASHED_OTP)
                .attemptCount(0)
                .maxAttempts(5)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .verified(false)
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.of(record));
        when(otpHasher.verifyBcrypt("000000", HASHED_OTP)).thenReturn(false);
        when(otpRepository.update(any(OtpRecord.class))).thenAnswer(i -> i.getArgument(0));

        // When
        OtpResponse response = otpService.validateOtp(request);

        // Then
        assertThat(response.getValid()).isFalse();
        assertThat(response.getRemainingAttempts()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should throw exception when OTP expired")
    void shouldThrowExceptionWhenOtpExpired() {
        // Given
        OtpValidateRequest request = OtpValidateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otp(OTP)
                .build();

        OtpRecord record = OtpRecord.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // Expired
                .verified(false)
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.of(record));

        // When & Then
        assertThatThrownBy(() -> otpService.validateOtp(request))
                .isInstanceOf(OtpException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OTP_EXPIRED);
    }

    @Test
    @DisplayName("Should throw exception when max attempts exceeded")
    void shouldThrowExceptionWhenMaxAttemptsExceeded() {
        // Given
        OtpValidateRequest request = OtpValidateRequest.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .otp(OTP)
                .build();

        OtpRecord record = OtpRecord.builder()
                .identifier(PHONE)
                .purpose(OtpPurpose.LOGIN)
                .attemptCount(5)
                .maxAttempts(5)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .verified(false)
                .build();

        when(otpRepository.findByIdentifierAndPurpose(PHONE, OtpPurpose.LOGIN))
                .thenReturn(Optional.of(record));

        // When & Then
        assertThatThrownBy(() -> otpService.validateOtp(request))
                .isInstanceOf(OtpException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OTP_MAX_ATTEMPTS);
    }
}
