package com.scrap.gain.otpmodule.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrap.gain.otpmodule.dto.OtpGenerateRequest;
import com.scrap.gain.otpmodule.model.OtpPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("OTP Controller Integration Tests")
class OtpControllerIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should generate OTP via API")
    void shouldGenerateOtpViaApi() throws Exception {
        OtpGenerateRequest request = OtpGenerateRequest.builder()
                .identifier("+919876543210")
                .purpose(OtpPurpose.LOGIN)
                .build();

        mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceId").exists())
                .andExpect(jsonPath("$.maskedIdentifier").value("+91****3210"))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    @Test
    @DisplayName("Should return 400 for invalid phone format")
    void shouldReturn400ForInvalidPhoneFormat() throws Exception {
        OtpGenerateRequest request = OtpGenerateRequest.builder()
                .identifier("invalid-phone")
                .purpose(OtpPurpose.LOGIN)
                .build();

        mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing purpose")
    void shouldReturn400ForMissingPurpose() throws Exception {
        String request = """
            {
                "identifier": "+919876543210"
            }
            """;

        mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
