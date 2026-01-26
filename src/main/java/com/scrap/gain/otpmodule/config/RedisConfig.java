package com.scrap.gain.otpmodule.config;

import com.scrap.gain.otpmodule.entity.OtpRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, OtpRecord> otpRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, OtpRecord> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for OtpRecord values
        JacksonJsonRedisSerializer<OtpRecord> serializer =
                new JacksonJsonRedisSerializer<>(OtpRecord.class);
        template.setValueSerializer(serializer);

        // Hash key/value serializers
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
