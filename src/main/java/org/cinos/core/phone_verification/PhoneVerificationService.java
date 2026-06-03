package org.cinos.core.phone_verification;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneVerificationService {

    private record CodeEntry(String code, LocalDateTime expiresAt) {}

    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void sendCode(String phone) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        codes.put(phone, new CodeEntry(code, LocalDateTime.now().plusMinutes(5)));
        // TODO: reemplazar con proveedor SMS (ej. Twilio)
        System.out.printf("[PhoneVerification] Código para %s: %s%n", phone, code);
    }

    public boolean verifyCode(String phone, String code) {
        CodeEntry entry = codes.get(phone);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            codes.remove(phone);
            return false;
        }
        boolean match = entry.code().equals(code);
        if (match) codes.remove(phone);
        return match;
    }
}
