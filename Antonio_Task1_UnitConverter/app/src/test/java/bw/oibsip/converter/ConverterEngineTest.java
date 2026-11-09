package bw.oibsip.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.List;

public class ConverterEngineTest {

    private static ConverterEngine.Unit unit(ConverterEngine.Category category, String symbol) {
        List<ConverterEngine.Unit> units = ConverterEngine.units(category);
        for (ConverterEngine.Unit unit : units) {
            if (unit.symbol.equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("No unit " + symbol);
    }

    @Test
    public void centimetresToInches() {
        double result = ConverterEngine.convert(100,
                unit(ConverterEngine.Category.LENGTH, "cm"),
                unit(ConverterEngine.Category.LENGTH, "in"));
        assertEquals(39.37007874d, result, 0.000001d);
    }

    @Test
    public void milesToKilometres() {
        double result = ConverterEngine.convert(1,
                unit(ConverterEngine.Category.LENGTH, "mi"),
                unit(ConverterEngine.Category.LENGTH, "km"));
        assertEquals(1.609344d, result, 1e-9);
    }

    @Test
    public void identicalUnitsPassThrough() {
        double result = ConverterEngine.convert(12.5,
                unit(ConverterEngine.Category.WEIGHT, "kg"),
                unit(ConverterEngine.Category.WEIGHT, "kg"));
        assertEquals(12.5d, result, 0d);
    }

    @Test
    public void kilogramsToPoundsIsExactByDefinition() {
        double result = ConverterEngine.convert(2.5,
                unit(ConverterEngine.Category.WEIGHT, "kg"),
                unit(ConverterEngine.Category.WEIGHT, "lb"));
        assertEquals(2.5d * 1000d / 453.59237d, result, 1e-9);
    }

    @Test
    public void usGallonToLitres() {
        double result = ConverterEngine.convert(1,
                unit(ConverterEngine.Category.VOLUME, "US gal"),
                unit(ConverterEngine.Category.VOLUME, "L"));
        assertEquals(3.785411784d, result, 1e-9);
    }

    @Test
    public void celsiusToFahrenheit() {
        assertEquals(32d, ConverterEngine.convert(0,
                unit(ConverterEngine.Category.TEMPERATURE, "°C"),
                unit(ConverterEngine.Category.TEMPERATURE, "°F")), 1e-9);
        assertEquals(212d, ConverterEngine.convert(100,
                unit(ConverterEngine.Category.TEMPERATURE, "°C"),
                unit(ConverterEngine.Category.TEMPERATURE, "°F")), 1e-9);
        assertEquals(-40d, ConverterEngine.convert(-40,
                unit(ConverterEngine.Category.TEMPERATURE, "°C"),
                unit(ConverterEngine.Category.TEMPERATURE, "°F")), 1e-9);
    }

    @Test
    public void kelvinAndZeroCelsius() {
        assertEquals(273.15d, ConverterEngine.convert(0,
                unit(ConverterEngine.Category.TEMPERATURE, "°C"),
                unit(ConverterEngine.Category.TEMPERATURE, "K")), 1e-9);
        assertEquals(-273.15d, ConverterEngine.convert(0,
                unit(ConverterEngine.Category.TEMPERATURE, "K"),
                unit(ConverterEngine.Category.TEMPERATURE, "°C")), 1e-9);
    }

    @Test
    public void absoluteZeroIsAllowedButBelowItIsNot() {
        ConverterEngine.Unit celsius = unit(ConverterEngine.Category.TEMPERATURE, "°C");
        ConverterEngine.Unit fahrenheit = unit(ConverterEngine.Category.TEMPERATURE, "°F");
        assertFalse(ConverterEngine.belowAbsoluteZero(-273.15d, celsius));
        assertTrue(ConverterEngine.belowAbsoluteZero(-273.16d, celsius));
        assertFalse(ConverterEngine.belowAbsoluteZero(-459.67d, fahrenheit));
        assertTrue(ConverterEngine.belowAbsoluteZero(-459.68d, fahrenheit));
    }

    @Test
    public void parsesDecimalCommaAndGrouping() {
        assertEquals(1.5d, ConverterEngine.parseInput("1,5"), 0d);
        assertEquals(1234.5d, ConverterEngine.parseInput("1,234.5"), 0d);
        assertEquals(-0.25d, ConverterEngine.parseInput(" -0.25 "), 0d);
        assertEquals(7d, ConverterEngine.parseInput("7."), 0d);
    }

    @Test
    public void rejectsNonNumbers() {
        String[] bad = {"", "   ", "abc", "1.2.3", "1e5", "NaN", "Infinity", "-", "."};
        for (String raw : bad) {
            try {
                ConverterEngine.parseInput(raw);
                fail("Should have rejected: " + raw);
            } catch (NumberFormatException expected) {
                // correct
            }
        }
    }

    @Test
    public void formatsForTheReadout() {
        assertEquals("0.3", ConverterEngine.format(0.1d + 0.2d));
        assertEquals("1,234,567.891", ConverterEngine.format(1234567.891d));
        assertEquals("0", ConverterEngine.format(0d));
        assertEquals("0", ConverterEngine.format(-0.00000001d));
        assertEquals("1.000000E+13", ConverterEngine.format(1e13d));
        assertEquals("Out of range", ConverterEngine.format(Double.NaN));
    }

    @Test
    public void allCategoriesHaveAtLeastTwoUnits() {
        for (ConverterEngine.Category category : ConverterEngine.Category.values()) {
            assertTrue(category.name(), ConverterEngine.units(category).size() >= 2);
        }
        assertEquals(26,
                ConverterEngine.units(ConverterEngine.Category.LENGTH).size()
                        + ConverterEngine.units(ConverterEngine.Category.WEIGHT).size()
                        + ConverterEngine.units(ConverterEngine.Category.VOLUME).size()
                        + ConverterEngine.units(ConverterEngine.Category.TEMPERATURE).size());
    }

    @Test
    public void sentenceReadsLikeAPlainLine() {
        ConverterEngine.Unit from = unit(ConverterEngine.Category.LENGTH, "cm");
        ConverterEngine.Unit to = unit(ConverterEngine.Category.LENGTH, "in");
        String sentence = ConverterEngine.sentence(250, from, to, "98.425197");
        assertEquals("250 cm = 98.425197 in", sentence);
    }
}
