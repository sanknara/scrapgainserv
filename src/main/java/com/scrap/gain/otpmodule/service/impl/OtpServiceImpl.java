package com.scrap.gain.otpmodule.service.impl;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import com.scrap.gain.otpmodule.dto.OtpGenerateRequest;
import com.scrap.gain.otpmodule.dto.OtpResendRequest;
import com.scrap.gain.otpmodule.dto.OtpResponse;
import com.scrap.gain.otpmodule.dto.OtpValidateRequest;
import com.scrap.gain.otpmodule.entity.OtpRecord;
import com.scrap.gain.otpmodule.exception.ErrorCode;
import com.scrap.gain.otpmodule.exception.OtpException;
import com.scrap.gain.otpmodule.repository.OtpRepository;
import com.scrap.gain.otpmodule.service.OtpService;
import com.scrap.gain.otpmodule.service.SmsService;
import com.scrap.gain.otpmodule.util.MaskingUtil;
import com.scrap.gain.otpmodule.util.OtpGenerator;
import com.scrap.gain.otpmodule.util.OtpHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;
    private final OtpHasher otpHasher;
    private final MaskingUtil maskingUtil;
    private final OtpConfigProperties config;
    private final SmsService smsService;

    @Override
    public OtpResponse generateOtp(OtpGenerateRequest request) {
        log.info("Generating OTP for identifier: {}, purpose: {}",
                maskingUtil.maskIdentifier(request.getIdentifier()),
                request.getPurpose());

        // Check if OTP already exists and is still valid
        otpRepository.findByIdentifierAndPurpose(request.getIdentifier(), request.getPurpose())
                .ifPresent(existing -> {
                    if (!existing.isExpired()) {
                        log.warn("OTP already sent for {}", maskingUtil.maskIdentifier(request.getIdentifier()));
                        throw new OtpException(ErrorCode.OTP_ALREADY_SENT);
                    }
                });

        // Generate new OTP
        String plainOtp = otpGenerator.generate();
        String hashedOtp = otpHasher.hashWithBcrypt(plainOtp);

        // Create OTP record
        Instant now = Instant.now();
        OtpRecord record = OtpRecord.builder()
                .id(OtpRecord.generateId())
                .identifier(request.getIdentifier())
                .otpHash(hashedOtp)
                .purpose(request.getPurpose())
                .attemptCount(0)
                .maxAttempts(config.getMaxAttempts())
                .createdAt(now)
                .expiresAt(now.plus(config.getExpiryMinutes(), ChronoUnit.MINUTES))
                .verified(false)
                .metadata(request.getMetadata())
                .build();

        // Save to Redis
        otpRepository.save(record);

        // Send OTP via SMS
        smsService.sendOtp(request.getIdentifier(), plainOtp);

        log.info("OTP generated and sent successfully for {}",
                maskingUtil.maskIdentifier(request.getIdentifier()));

        return OtpResponse.success(
                record.getId(),
                maskingUtil.maskIdentifier(request.getIdentifier()),
                record.getExpiresAt()
        );
    }

    @Override
    public OtpResponse validateOtp(OtpValidateRequest request) {
        log.info("Validating OTP for identifier: {}, purpose: {}",
                maskingUtil.maskIdentifier(request.getIdentifier()),
                request.getPurpose());

        // Find OTP record
        OtpRecord record = otpRepository
                .findByIdentifierAndPurpose(request.getIdentifier(), request.getPurpose())
                .orElseThrow(() -> new OtpException(ErrorCode.OTP_NOT_FOUND));

        // Check if already verified
        if (record.isVerified()) {
            throw new OtpException(ErrorCode.OTP_ALREADY_VERIFIED);
        }

        // Check if expired
        if (record.isExpired()) {
            otpRepository.delete(request.getIdentifier(), request.getPurpose());
            throw new OtpException(ErrorCode.OTP_EXPIRED);
        }

        // Check max attempts
        if (record.isMaxAttemptsExceeded()) {
            otpRepository.delete(request.getIdentifier(), request.getPurpose());
            throw new OtpException(ErrorCode.OTP_MAX_ATTEMPTS);
        }

        // Verify OTP
        boolean isValid = otpHasher.verifyBcrypt(request.getOtp(), record.getOtpHash());

        if (isValid) {
            // Mark as verified and delete
            record.setVerified(true);
            otpRepository.delete(request.getIdentifier(), request.getPurpose());

            // Generate verification token
            String token = UUID.randomUUID().toString();

            log.info("OTP validated successfully for {}",
                    maskingUtil.maskIdentifier(request.getIdentifier()));

            return OtpResponse.validationSuccess(token);
        } else {
            // Increment attempt count
            record.incrementAttempts();
            otpRepository.update(record);

            int remaining = record.getMaxAttempts() - record.getAttemptCount();
            log.warn("Invalid OTP attempt for {}, remaining: {}",
                    maskingUtil.maskIdentifier(request.getIdentifier()), remaining);

            return OtpResponse.validationFailed(remaining, ErrorCode.OTP_INVALID.getCode());
        }
    }

    @Override
    public OtpResponse resendOtp(OtpResendRequest request) {
        log.info("Resending OTP for identifier: {}, purpose: {}",
                maskingUtil.maskIdentifier(request.getIdentifier()),
                request.getPurpose());

        // Delete existing OTP if any
        otpRepository.delete(request.getIdentifier(), request.getPurpose());

        // Generate new OTP using the generate method
        OtpGenerateRequest generateRequest = OtpGenerateRequest.builder()
                .identifier(request.getIdentifier())
                .purpose(request.getPurpose())
                .build();

        return generateOtp(generateRequest);
    }
}
