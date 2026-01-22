package com.scrap.gain.otpmodule.service.sms;

/**
 * Interface for SMS providers
 * Allows easy switching between different SMS services
 */
public interface SmsProvider {

    /**
     * Send SMS to the specified phone number
     * @param phoneNumber Phone number with country code (e.g., +919876543210)
     * @param message SMS message content
     * @return true if sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);

    /**
     * Get provider name for logging
     */
    String getProviderName();

    /**
     * Check if provider is properly configured
     */
    boolean isConfigured();
}
