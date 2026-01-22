package com.scrap.gain.otpmodule.service.sms;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS SNS SMS Provider implementation
 * Requires AWS credentials and region in config
 */
@Slf4j
@Component("awsSnsSmsProvider")
@RequiredArgsConstructor
public class AwsSnsSmsProvider implements SmsProvider {

    private final OtpConfigProperties config;
    private SnsClient snsClient;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            try {
                var awsConfig = config.getSms().getAws();

                AwsBasicCredentials credentials = AwsBasicCredentials.create(
                        awsConfig.getAccessKey(),
                        awsConfig.getSecretKey()
                );

                snsClient = SnsClient.builder()
                        .region(Region.of(awsConfig.getRegion()))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .build();

                initialized = true;
                log.info("AWS SNS SMS provider initialized successfully");

            } catch (Exception e) {
                log.error("Failed to initialize AWS SNS: {}", e.getMessage());
                initialized = false;
            }
        } else {
            log.warn("AWS SNS SMS provider not configured - missing credentials");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!initialized || snsClient == null) {
            log.error("AWS SNS not initialized - cannot send SMS");
            return false;
        }

        try {
            // Set SMS attributes for transactional messages
            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                    .stringValue("Transactional")
                    .dataType("String")
                    .build());

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .messageAttributes(smsAttributes)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("SMS sent via AWS SNS. MessageId: {}", response.messageId());
            return true;

        } catch (Exception e) {
            log.error("Failed to send SMS via AWS SNS: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "AWS_SNS";
    }

    @Override
    public boolean isConfigured() {
        var aws = config.getSms().getAws();
        return StringUtils.hasText(aws.getAccessKey())
                && StringUtils.hasText(aws.getSecretKey());
    }
}
