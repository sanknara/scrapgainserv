package com.scrap.gain.otpmodule.service;

import com.scrap.gain.otpmodule.exception.ErrorCode;
import com.scrap.gain.otpmodule.exception.OtpException;
import com.scrap.gain.otpmodule.service.sms.SmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sending OTP via SMS
 * Delegates to configured SmsProvider
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsProvider smsProvider;

    private static final String OTP_MESSAGE_TEMPLATE =
            "Your OTP is: %s. Valid for 5 minutes. Do not share with anyone.";

    /**
     * Send OTP to phone number
     * @param phoneNumber Phone number with country code
     * @param otp Plain text OTP
     */
    public void sendOtp(String phoneNumber, String otp) {
        log.info("Sending OTP via {} to phone ending with ...{}",
                smsProvider.getProviderName(),
                phoneNumber.substring(phoneNumber.length() - 4));

        String message = String.format(OTP_MESSAGE_TEMPLATE, otp);

        boolean sent = smsProvider.sendSms(phoneNumber, message);

        if (!sent) {
            log.error("Failed to send OTP to {}", phoneNumber);
            throw new OtpException(ErrorCode.SMS_DELIVERY_FAILED);
        }

        log.info("OTP sent successfully via {}", smsProvider.getProviderName());
    }

    /**
     * Send custom message (for future use)
     */
    public void sendMessage(String phoneNumber, String message) {
        boolean sent = smsProvider.sendSms(phoneNumber, message);

        if (!sent) {
            throw new OtpException(ErrorCode.SMS_DELIVERY_FAILED);
        }
    }
}
