package com.scrap.gain.otpmodule.dto;

import com.scrap.gain.otpmodule.model.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResendRequest {

    @NotBlank(message = "Identifier (phone/email) is required")
    @Pattern(
            regexp = "^(\\+91[6-9]\\d{9}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
            message = "Invalid phone number or email format"
    )
    private String identifier;

    @NotNull(message = "OTP purpose is required")
    private OtpPurpose purpose;
}
