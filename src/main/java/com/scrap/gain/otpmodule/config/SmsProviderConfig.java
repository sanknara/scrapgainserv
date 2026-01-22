package com.scrap.gain.otpmodule.config;

import com.scrap.gain.otpmodule.service.sms.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to select SMS provider based on application properties
 */
@Slf4j
@Configuration
public class SmsProviderConfig {

    @Bean
    @Primary
    public SmsProvider smsProvider(
            OtpConfigProperties config,
            @Qualifier("mockSmsProvider") SmsProvider mockProvider,
            @Qualifier("twilioSmsProvider") SmsProvider twilioProvider,
            @Qualifier("awsSnsSmsProvider") SmsProvider awsSnsProvider) {

        String providerName = config.getSms().getProvider().toUpperCase();

        SmsProvider selectedProvider = switch (providerName) {
            case "TWILIO" -> {
                if (twilioProvider.isConfigured()) {
                    log.info("Using Twilio SMS provider");
                    yield twilioProvider;
                } else {
                    log.warn("Twilio not configured, falling back to MOCK");
                    yield mockProvider;
                }
            }
            case "AWS_SNS", "AWS" -> {
                if (awsSnsProvider.isConfigured()) {
                    log.info("Using AWS SNS SMS provider");
                    yield awsSnsProvider;
                } else {
                    log.warn("AWS SNS not configured, falling back to MOCK");
                    yield mockProvider;
                }
            }
            default -> {
                log.info("Using MOCK SMS provider");
                yield mockProvider;
            }
        };

        return selectedProvider;
    }
}
