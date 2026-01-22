package com.scrap.gain.otpmodule.repository;

import com.scrap.gain.otpmodule.config.OtpConfigProperties;
import com.scrap.gain.otpmodule.entity.OtpRecord;
import com.scrap.gain.otpmodule.model.OtpPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OtpRepository {

    private final RedisTemplate<String, OtpRecord> otpRedisTemplate;
    private final OtpConfigProperties config;

    /**
     * Save OTP record with TTL based on expiry configuration
     */
    public OtpRecord save(OtpRecord record) {
        String key = record.getRedisKey();
        Duration ttl = Duration.ofMinutes(config.getExpiryMinutes());

        otpRedisTemplate.opsForValue().set(key, record, ttl);
        log.debug("Saved OTP record with key: {}, TTL: {} minutes", key, config.getExpiryMinutes());

        return record;
    }

    /**
     * Find OTP record by identifier and purpose
     */
    public Optional<OtpRecord> findByIdentifierAndPurpose(String identifier, OtpPurpose purpose) {
        String key = OtpRecord.buildRedisKey(identifier, purpose);
        OtpRecord record = otpRedisTemplate.opsForValue().get(key);

        log.debug("Looking up OTP with key: {}, found: {}", key, record != null);
        return Optional.ofNullable(record);
    }

    /**
     * Delete OTP record (after successful verification or manual invalidation)
     */
    public void delete(String identifier, OtpPurpose purpose) {
        String key = OtpRecord.buildRedisKey(identifier, purpose);
        Boolean deleted = otpRedisTemplate.delete(key);
        log.debug("Deleted OTP with key: {}, success: {}", key, deleted);
    }

    /**
     * Delete OTP record by key
     */
    public void deleteByKey(String key) {
        otpRedisTemplate.delete(key);
        log.debug("Deleted OTP with key: {}", key);
    }

    /**
     * Check if OTP exists for identifier and purpose
     */
    public boolean exists(String identifier, OtpPurpose purpose) {
        String key = OtpRecord.buildRedisKey(identifier, purpose);
        return Boolean.TRUE.equals(otpRedisTemplate.hasKey(key));
    }

    /**
     * Update existing OTP record (e.g., increment attempts)
     */
    public OtpRecord update(OtpRecord record) {
        String key = record.getRedisKey();

        // Get remaining TTL
        Long ttl = otpRedisTemplate.getExpire(key);
        if (ttl != null && ttl > 0) {
            otpRedisTemplate.opsForValue().set(key, record, Duration.ofSeconds(ttl));
        } else {
            // If no TTL, use default
            otpRedisTemplate.opsForValue().set(key, record,
                    Duration.ofMinutes(config.getExpiryMinutes()));
        }

        log.debug("Updated OTP record with key: {}", key);
        return record;
    }
}
