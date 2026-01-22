package com.scrap.gain.otpmodule.util;

import org.springframework.stereotype.Component;

@Component
public class MaskingUtil {

    /**
     * Mask phone number: +919876543210 -> +91****3210
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) {
            return "****";
        }
        int len = phone.length();
        return phone.substring(0, len - 8) + "****" + phone.substring(len - 4);
    }

    /**
     * Mask email: john.doe@example.com -> j***e@example.com
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    /**
     * Automatically detect and mask identifier
     */
    public String maskIdentifier(String identifier) {
        if (identifier == null) return "****";
        if (identifier.contains("@")) {
            return maskEmail(identifier);
        }
        return maskPhone(identifier);
    }
}
