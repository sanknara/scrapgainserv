package com.scrap.gain.otpmodule.service.sms;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Twilio SMS Provider implementation
 * Requires twilio.account-sid, twilio.auth-token, twilio.from-number in config
 */
@Slf4j
@Component("twilioSmsProvider")
@RequiredArgsConstructor
public class TwilioSmsProvider implements SmsProvider {

    private final OtpConfigProperties config;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            try {
                Twilio.init(
                        config.getSms().getTwilio().getAccountSid(),
                        config.getSms().getTwilio().getAuthToken()
                );
                initialized = true;
                log.info("Twilio SMS provider initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio: {}", e.getMessage());
                initialized = false;
            }
        } else {
            log.warn("Twilio SMS provider not configured - missing credentials");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!initialized) {
            log.error("Twilio not initialized - cannot send SMS");
            return false;
        }

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(config.getSms().getTwilio().getFromNumber()),
                    message
            ).create();

            log.info("SMS sent via Twilio. SID: {}, Status: {}",
                    twilioMessage.getSid(), twilioMessage.getStatus());

            return twilioMessage.getStatus() != Message.Status.FAILED;

        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }

    @Override
    public boolean isConfigured() {
        var twilio = config.getSms().getTwilio();
        return StringUtils.hasText(twilio.getAccountSid())
                && StringUtils.hasText(twilio.getAuthToken())
                && StringUtils.hasText(twilio.getFromNumber());
    }
}
