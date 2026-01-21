package com.scrap.gain.otpmodule.dto;

import com.scrap.gain.otpmodule.model.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpValidateRequest {

    @NotBlank(message = "Identifier (phone/email) is required")
    @Pattern(
            regexp = "^(\\+91[6-9]\\d{9}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
            message = "Invalid phone number or email format"
    )
    private String identifier;

    @NotNull(message = "OTP purpose is required")
    private OtpPurpose purpose;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must contain only digits")
    private String otp;
}
