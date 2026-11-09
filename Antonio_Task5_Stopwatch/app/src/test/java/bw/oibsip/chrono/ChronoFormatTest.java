package bw.oibsip.chrono;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChronoFormatTest {

    @Test
    public void minutesAndSecondsUnderAnHour() {
        assertEquals("00:00", ChronoFormat.main(0L));
        assertEquals("00:42", ChronoFormat.main(42_000L));
        assertEquals("59:59", ChronoFormat.main(3_599_000L));
    }

    @Test
    public void hoursAppearOnlyWhenNeeded() {
        assertEquals("1:00:00", ChronoFormat.main(3_600_000L));
        assertEquals("1:02:03", ChronoFormat.main(3_723_123L));
        assertEquals("100:00:00", ChronoFormat.main(360_000_000L));
    }

    @Test
    public void hundredthsAreZeroPadded() {
        assertEquals(".00", ChronoFormat.fraction(0L));
        assertEquals(".05", ChronoFormat.fraction(50L));
        assertEquals(".23", ChronoFormat.fraction(4_237L));
        assertEquals(".99", ChronoFormat.fraction(999L));
    }

    @Test
    public void fullReadoutCombinesBothParts() {
        assertEquals("00:00.00", ChronoFormat.full(0L));
        assertEquals("00:42.12", ChronoFormat.full(42_123L));
        assertEquals("1:02:03.12", ChronoFormat.full(3_723_123L));
    }

    @Test
    public void negativeInputIsClampedNotPrintedAsMinus() {
        assertEquals("00:00.00", ChronoFormat.full(-500L));
    }
}
