package com.scrap.gain.otpmodule.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties(prefix = "otp")
@Validated
public class OtpConfigProperties {

    @Min(4) @Max(8)
    private int length = 6;

    @Min(1) @Max(30)
    private int expiryMinutes = 5;

    @Min(1) @Max(10)
    private int maxAttempts = 5;

    @NotNull
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @NotNull
    private SmsConfig sms = new SmsConfig();

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int requestsPerMinute = 3;
        private int requestsPerHour = 10;
    }

    @Data
    public static class SmsConfig {
        private String provider = "MOCK"; // MOCK, TWILIO, AWS_SNS
        private TwilioConfig twilio = new TwilioConfig();
        private AwsSnsConfig aws = new AwsSnsConfig();
    }

    @Data
    public static class TwilioConfig {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }

    @Data
    public static class AwsSnsConfig {
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;
    }
}
