package bw.oibsip.ledger;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password storage for the ledger.
 *
 * The assignment asks for "basic hashing". A plain MD5/SHA digest is the usual shortcut,
 * and it is the wrong one: one leaked table lets an attacker reverse every password in
 * minutes. This uses PBKDF2-HMAC-SHA256 with a per-account 16-byte salt and 120,000
 * iterations, which is still dependency-free (it ships with the platform) but is a real
 * password hash rather than a checksum.
 *
 * Pure Java on purpose: no android.* imports, so it is covered by JVM unit tests.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** A fresh 16-byte salt, Base64 encoded so it can live in a TEXT column. */
    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("password and salt are required");
        }
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is unavailable on this device", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time comparison, so a wrong password cannot be found byte by byte. */
    public static boolean verify(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }
        byte[] expected;
        try {
            expected = Base64.getDecoder().decode(expectedHash);
        } catch (IllegalArgumentException malformed) {
            return false;
        }
        byte[] actual;
        try {
            actual = Base64.getDecoder().decode(hash(password, salt));
        } catch (IllegalArgumentException malformedSalt) {
            return false;
        }
        return MessageDigest.isEqual(expected, actual);
    }
}
