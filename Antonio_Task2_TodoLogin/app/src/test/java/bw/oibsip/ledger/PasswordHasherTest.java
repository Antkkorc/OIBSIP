package bw.oibsip.ledger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Base64;

public class PasswordHasherTest {

    @Test
    public void hashIsNeverThePassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("correct horse battery", salt);
        assertNotEquals("correct horse battery", hash);
        assertFalse(hash.contains("correct"));
    }

    @Test
    public void verifyAcceptsTheRightPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("passphrase-2026", salt);
        assertTrue(PasswordHasher.verify("passphrase-2026", salt, hash));
    }

    @Test
    public void verifyRejectsTheWrongPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("passphrase-2026", salt);
        assertFalse(PasswordHasher.verify("passphrase-2025", salt, hash));
        assertFalse(PasswordHasher.verify("", salt, hash));
        assertFalse(PasswordHasher.verify(null, salt, hash));
    }

    @Test
    public void saltsAreUniqueAndSixteenBytes() {
        String first = PasswordHasher.newSalt();
        String second = PasswordHasher.newSalt();
        assertNotEquals(first, second);
        assertEquals(16, Base64.getDecoder().decode(first).length);
    }

    @Test
    public void samePasswordDifferentSaltProducesDifferentHash() {
        String a = PasswordHasher.hash("same-password", PasswordHasher.newSalt());
        String b = PasswordHasher.hash("same-password", PasswordHasher.newSalt());
        assertNotEquals(a, b);
    }

    @Test
    public void malformedStoredHashFailsClosed() {
        String salt = PasswordHasher.newSalt();
        assertFalse(PasswordHasher.verify("whatever", salt, "not base64 at all!!"));
        assertFalse(PasswordHasher.verify("whatever", "also-not-base64!!", "AAAA"));
    }
}
