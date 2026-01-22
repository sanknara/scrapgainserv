package com.scrap.gain.otpmodule.service;

import com.scrap.gain.otpmodule.dto.OtpGenerateRequest;
import com.scrap.gain.otpmodule.dto.OtpResendRequest;
import com.scrap.gain.otpmodule.dto.OtpResponse;
import com.scrap.gain.otpmodule.dto.OtpValidateRequest;

public interface OtpService {

    /**
     * Generate and send OTP to the specified identifier
     */
    OtpResponse generateOtp(OtpGenerateRequest request);

    /**
     * Validate the OTP provided by user
     */
    OtpResponse validateOtp(OtpValidateRequest request);

    /**
     * Resend OTP (invalidates previous and generates new)
     */
    OtpResponse resendOtp(OtpResendRequest request);
}
