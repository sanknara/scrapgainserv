package com.scrap.gain.otpmodule.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock SMS Provider for development and testing
 * Logs OTP to console instead of sending actual SMS
 */
@Slf4j
@Component("mockSmsProvider")
public class MockSmsProvider implements SmsProvider {

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        log.info("========================================");
        log.info("📱 MOCK SMS SENT");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("========================================");

        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public boolean isConfigured() {
        return true; // Always configured
    }
}
