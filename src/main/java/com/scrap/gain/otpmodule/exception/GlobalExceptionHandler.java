package com.scrap.gain.otpmodule.exception;

import com.scrap.gain.otpmodule.dto.OtpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST API
 * Converts exceptions to standardized OtpResponse format
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle OTP-specific exceptions
     */
    @ExceptionHandler(OtpException.class)
    public ResponseEntity<OtpResponse> handleOtpException(OtpException ex) {
        log.error("OTP Exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());

        OtpResponse response = OtpResponse.error(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getMessage()
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OtpResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", errors);

        OtpResponse response = OtpResponse.error(
                ErrorCode.INVALID_REQUEST.getCode(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<OtpResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        OtpResponse response = OtpResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected error occurred"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Map error codes to HTTP status codes
     */
    private HttpStatus mapErrorCodeToStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case OTP_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case OTP_EXPIRED, OTP_INVALID, OTP_MAX_ATTEMPTS,
                 OTP_ALREADY_VERIFIED, OTP_ALREADY_SENT -> HttpStatus.BAD_REQUEST;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case INVALID_PHONE_FORMAT, INVALID_EMAIL_FORMAT,
                 INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case SMS_DELIVERY_FAILED, SMS_PROVIDER_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
