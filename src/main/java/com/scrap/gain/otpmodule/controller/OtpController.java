package com.scrap.gain.otpmodule.controller;

import com.scrap.gain.otpmodule.dto.OtpGenerateRequest;
import com.scrap.gain.otpmodule.dto.OtpResendRequest;
import com.scrap.gain.otpmodule.dto.OtpResponse;
import com.scrap.gain.otpmodule.dto.OtpValidateRequest;
import com.scrap.gain.otpmodule.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for OTP operations
 *
 * Endpoints:
 * - POST /api/v1/otp/generate - Generate and send OTP
 * - POST /api/v1/otp/validate - Validate OTP
 * - POST /api/v1/otp/resend   - Resend OTP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
//    private final RateLimitService rateLimitService;

    /**
     * Generate and send OTP to the specified identifier
     *
     * @param request Contains identifier (phone/email) and purpose
     * @return OtpResponse with reference ID and expiry time
     */
    @PostMapping("/generate")
    public ResponseEntity<OtpResponse> generateOtp(
            @Valid @RequestBody OtpGenerateRequest request) {

        log.info("POST /api/v1/otp/generate - identifier: {}, purpose: {}",
                maskIdentifier(request.getIdentifier()), request.getPurpose());

        // Check rate limit - TBD
//        if (!rateLimitService.isAllowed(request.getIdentifier())) {
//            throw new OtpException(ErrorCode.RATE_LIMIT_EXCEEDED);
//        }

        OtpResponse response = otpService.generateOtp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Validate OTP provided by user
     *
     * @param request Contains identifier, purpose, and OTP
     * @return OtpResponse with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<OtpResponse> validateOtp(
            @Valid @RequestBody OtpValidateRequest request) {

        log.info("POST /api/v1/otp/validate - identifier: {}, purpose: {}",
                maskIdentifier(request.getIdentifier()), request.getPurpose());

        OtpResponse response = otpService.validateOtp(request);

        HttpStatus status = Boolean.TRUE.equals(response.getValid())
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Resend OTP (invalidates previous OTP)
     *
     * @param request Contains identifier and purpose
     * @return OtpResponse with new reference ID and expiry time
     */
    @PostMapping("/resend")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody OtpResendRequest request) {

        log.info("POST /api/v1/otp/resend - identifier: {}, purpose: {}",
                maskIdentifier(request.getIdentifier()), request.getPurpose());

        // Check rate limit
//        if (!rateLimitService.isAllowed(request.getIdentifier())) {
//            throw new OtpException(ErrorCode.RATE_LIMIT_EXCEEDED);
//        }

        OtpResponse response = otpService.resendOtp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OTP Service is running");
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "****";
        return "****" + identifier.substring(identifier.length() - 4);
    }
}
