package com.scrap.gain.otpmodule.exception;

import lombok.Getter;

@Getter
public class OtpException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public OtpException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public OtpException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    public OtpException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = cause.getMessage();
    }
}
