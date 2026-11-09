package bw.oibsip.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * All conversion maths lives here, with no Android imports, so it can be unit tested
 * on the JVM without an emulator.
 *
 * Every non-temperature unit is stored as a factor to a category base unit
 * (metre, gram, litre). Temperature is handled by an explicit Celsius round-trip
 * because scales with offsets cannot be expressed as a single multiplier.
 */
public final class ConverterEngine {

    public enum Category {
        LENGTH("Length"),
        WEIGHT("Weight"),
        VOLUME("Volume"),
        TEMPERATURE("Temperature");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    /** One selectable unit: what it is called, how it is written, and its factor to base. */
    public static final class Unit {
        public final Category category;
        public final String name;
        public final String symbol;
        /** Multiplier to the category base unit. Unused for temperature. */
        public final double toBase;

        Unit(Category category, String name, String symbol, double toBase) {
            this.category = category;
            this.name = name;
            this.symbol = symbol;
            this.toBase = toBase;
        }

        public String menuLabel() {
            return name + " · " + symbol;
        }

        public String symbolUpper() {
            return symbol.toUpperCase(Locale.US);
        }

        @Override
        public String toString() {
            return menuLabel();
        }
    }

    private static final double ABSOLUTE_ZERO_C = -273.15d;
    private static final Pattern NUMBER =
            Pattern.compile("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)$");

    private static final DecimalFormat PLAIN =
            new DecimalFormat("#,##0.######", DecimalFormatSymbols.getInstance(Locale.US));

    static {
        PLAIN.setRoundingMode(java.math.RoundingMode.HALF_UP);
    }

    private static final Map<Category, List<Unit>> UNITS = new EnumMap<>(Category.class);

    static {
        UNITS.put(Category.LENGTH, Collections.unmodifiableList(Arrays.asList(
                new Unit(Category.LENGTH, "Millimetre", "mm", 0.001d),
                new Unit(Category.LENGTH, "Centimetre", "cm", 0.01d),
                new Unit(Category.LENGTH, "Metre", "m", 1d),
                new Unit(Category.LENGTH, "Kilometre", "km", 1000d),
                new Unit(Category.LENGTH, "Inch", "in", 0.0254d),
                new Unit(Category.LENGTH, "Foot", "ft", 0.3048d),
                new Unit(Category.LENGTH, "Yard", "yd", 0.9144d),
                new Unit(Category.LENGTH, "Mile", "mi", 1609.344d)
        )));

        UNITS.put(Category.WEIGHT, Collections.unmodifiableList(Arrays.asList(
                new Unit(Category.WEIGHT, "Milligram", "mg", 0.001d),
                new Unit(Category.WEIGHT, "Gram", "g", 1d),
                new Unit(Category.WEIGHT, "Kilogram", "kg", 1000d),
                new Unit(Category.WEIGHT, "Tonne", "t", 1000000d),
                new Unit(Category.WEIGHT, "Ounce", "oz", 28.349523125d),
                new Unit(Category.WEIGHT, "Pound", "lb", 453.59237d),
                new Unit(Category.WEIGHT, "Stone", "st", 6350.29318d)
        )));

        UNITS.put(Category.VOLUME, Collections.unmodifiableList(Arrays.asList(
                new Unit(Category.VOLUME, "Millilitre", "mL", 0.001d),
                new Unit(Category.VOLUME, "Litre", "L", 1d),
                new Unit(Category.VOLUME, "Cubic metre", "m³", 1000d),
                new Unit(Category.VOLUME, "US gallon", "US gal", 3.785411784d),
                new Unit(Category.VOLUME, "US quart", "US qt", 0.946352946d),
                new Unit(Category.VOLUME, "US pint", "US pt", 0.473176473d),
                new Unit(Category.VOLUME, "US fluid ounce", "US fl oz", 0.0295735295625d),
                new Unit(Category.VOLUME, "US cup", "US cup", 0.2365882365d)
        )));

        UNITS.put(Category.TEMPERATURE, Collections.unmodifiableList(Arrays.asList(
                new Unit(Category.TEMPERATURE, "Celsius", "°C", 1d),
                new Unit(Category.TEMPERATURE, "Fahrenheit", "°F", 1d),
                new Unit(Category.TEMPERATURE, "Kelvin", "K", 1d)
        )));
    }

    private ConverterEngine() {
    }

    public static List<Unit> units(Category category) {
        List<Unit> list = UNITS.get(category);
        return list == null ? Collections.<Unit>emptyList() : list;
    }

    public static List<String> menuLabels(Category category) {
        List<String> labels = new ArrayList<>();
        for (Unit unit : units(category)) {
            labels.add(unit.menuLabel());
        }
        return labels;
    }

    public static String baseName(Category category) {
        switch (category) {
            case LENGTH:
                return "metre";
            case WEIGHT:
                return "gram";
            case VOLUME:
                return "litre";
            case TEMPERATURE:
            default:
                return "celsius";
        }
    }

    /** Converts a value between two units of the same category. */
    public static double convert(double value, Unit from, Unit to) {
        if (from.category != to.category) {
            throw new IllegalArgumentException("Units belong to different categories");
        }
        if (from.category == Category.TEMPERATURE) {
            double celsius = toCelsius(value, from.symbol);
            return fromCelsius(celsius, to.symbol);
        }
        return value * from.toBase / to.toBase;
    }

    private static double toCelsius(double value, String symbol) {
        if ("°F".equals(symbol)) {
            return (value - 32d) * 5d / 9d;
        }
        if ("K".equals(symbol)) {
            return value - 273.15d;
        }
        return value;
    }

    private static double fromCelsius(double celsius, String symbol) {
        if ("°F".equals(symbol)) {
            return celsius * 9d / 5d + 32d;
        }
        if ("K".equals(symbol)) {
            return celsius + 273.15d;
        }
        return celsius;
    }

    /**
     * True when a temperature input sits below absolute zero.
     * A tolerance of 1e-9 keeps -459.67 °F (exactly absolute zero) valid.
     */
    public static boolean belowAbsoluteZero(double value, Unit from) {
        if (from.category != Category.TEMPERATURE) {
            return false;
        }
        return toCelsius(value, from.symbol) < ABSOLUTE_ZERO_C - 1e-9;
    }

    /**
     * Parses a human-typed number. A single comma without a dot is read as a decimal
     * separator (1,5 → 1.5); commas mixed with a dot are read as thousands separators.
     *
     * @throws NumberFormatException when the text is empty, incomplete or not a number
     */
    public static double parseInput(String raw) {
        if (raw == null) {
            throw new NumberFormatException("empty");
        }
        String s = raw.trim().replace('\u00A0', ' ').replace(" ", "");
        if (s.isEmpty()) {
            throw new NumberFormatException("empty");
        }
        if (s.indexOf('.') < 0 && s.indexOf(',') >= 0) {
            s = s.replace(',', '.');
        } else {
            s = s.replace(",", "");
        }
        if (!NUMBER.matcher(s).matches()) {
            throw new NumberFormatException("not a number: " + raw);
        }
        return Double.parseDouble(s);
    }

    /**
     * Instrument formatting: grouping separators, at most six decimals, and scientific
     * notation at the extremes so the readout never becomes an unreadable wall of digits.
     */
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "—";
        }
        double abs = Math.abs(value);
        if (abs < 5e-7d) {
            return "0";
        }
        if (abs >= 1e12d || abs < 1e-6d) {
            return String.format(Locale.US, "%.6E", value);
        }
        return PLAIN.format(value);
    }

    /** e.g. "250 CM = 8.2021 FT", kept uppercase so it reads as a stamped legend. */
    public static String sentence(double input, Unit from, Unit to, String resultText) {
        return format(input).toUpperCase(Locale.US) + " " + from.symbolUpper()
                + "  =  " + resultText + " " + to.symbolUpper();
    }
}
