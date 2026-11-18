package com.eventease.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:EventEaseSuperSecretSecurityKey2026WithSufficientLengthForHMACSHA256SignatureVerification}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    public String generateToken(String email, Long userId, String role, String name) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + jwtExpirationMs;

        String headerBase64 = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));

        String sanitizedEmail = escapeJson(email);
        String sanitizedRole = escapeJson(role);
        String sanitizedName = escapeJson(name != null ? name : "");

        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"name\":\"%s\",\"iat\":%d,\"exp\":%d}",
                sanitizedEmail,
                userId != null ? userId : 0L,
                sanitizedRole,
                sanitizedName,
                nowMillis / 1000,
                expMillis / 1000
        );

        String payloadBase64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String contentToSign = headerBase64 + "." + payloadBase64;
        String signatureBase64 = signHmacSha256(contentToSign, jwtSecret);

        return contentToSign + "." + signatureBase64;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String contentToSign = parts[0] + "." + parts[1];
            String expectedSignature = signHmacSha256(contentToSign, jwtSecret);

            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Long exp = extractLongField(payloadJson, "exp");
            if (exp == null) {
                return false;
            }

            long currentSeconds = Instant.now().getEpochSecond();
            return exp > currentSeconds;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractPayloadField(token, "sub");
    }

    public Long getUserIdFromToken(String token) {
        String payloadJson = getPayloadJson(token);
        return extractLongField(payloadJson, "userId");
    }

    public String getRoleFromToken(String token) {
        return extractPayloadField(token, "role");
    }

    private String getPayloadJson(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return "{}";
        }
        return new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
    }

    private String extractPayloadField(String token, String field) {
        String payloadJson = getPayloadJson(token);
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(payloadJson);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Long extractLongField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private String signHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signedBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT token", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
