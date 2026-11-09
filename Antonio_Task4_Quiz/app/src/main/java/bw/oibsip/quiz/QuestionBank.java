package bw.oibsip.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Twelve questions on coastal navigation, hardcoded so the app needs no network
 * permission and works offline on a boat, which is the only place it matters.
 *
 * Correct answers are deliberately spread across A, B, C and D in the source array —
 * if the shuffler ever broke, the app would still not give the game away.
 */
public final class QuestionBank {

    private QuestionBank() {
    }

    public static List<Question> all() {
        List<Question> bank = new ArrayList<>();

        bank.add(new Question(
                "One minute of latitude spans how far across the Earth's surface?",
                new String[]{"One nautical mile", "One kilometre", "One statute mile",
                        "One degree of longitude"},
                0,
                "By definition one minute of latitude is one nautical mile, 1,852 m. That is why "
                        + "distance on a paper chart is measured against the latitude scale at the "
                        + "side, never the longitude scale at the top."));

        bank.add(new Question(
                "The international nautical mile is defined as exactly",
                new String[]{"1,609.344 metres", "1,829 metres", "1,852 metres", "1,000 metres"},
                2,
                "Fixed at 1,852 m by the 1929 International Hydrographic Conference. The old "
                        + "British Admiralty mile was 1,853.184 m, which is why very old charts "
                        + "disagree with a GPS fix by a few metres per mile."));

        bank.add(new Question(
                "A knot is",
                new String[]{"One statute mile per hour", "One hundred fathoms per hour",
                        "One metre per second", "One nautical mile per hour"},
                3,
                "Ten knots is ten nautical miles an hour, about 18.5 km/h. Speed logs and GPS "
                        + "both report knots, and charts are scaled in nautical miles, so the unit "
                        + "does the arithmetic for you."));

        bank.add(new Question(
                "Which instrument measures the angle between the horizon and a celestial body?",
                new String[]{"Chronometer", "Sextant", "Barograph", "Lead line"},
                1,
                "A sextant reads that angle to a tenth of a minute of arc. The chronometer "
                        + "supplies the time and the time supplies longitude; the sextant alone "
                        + "only gives latitude."));

        bank.add(new Question(
                "A chronometer set to GMT is used to find",
                new String[]{"Latitude", "Depth", "Longitude", "Magnetic variation"},
                2,
                "Longitude is a time problem. The Earth turns 15 degrees every hour, so one hour "
                        + "of chronometer error becomes 15 degrees of longitude, which is up to 900 "
                        + "nautical miles at the equator."));

        bank.add(new Question(
                "Which star sits within one degree of the north celestial pole?",
                new String[]{"Sirius", "Vega", "Canopus", "Polaris"},
                3,
                "Polaris is about 0.65 degrees from the pole today, so its altitude is a usable "
                        + "latitude in the northern hemisphere. The southern hemisphere has no "
                        + "equivalent bright pole star, which is why the south was mapped later."));

        bank.add(new Question(
                "Under IALA Region A, the mark on the port side of a channel entering from seaward is",
                new String[]{"Green", "Red", "Yellow", "White"},
                1,
                "Region B — the Americas, Japan, Korea and the Philippines — reverses red and "
                        + "green. Mixing the two conventions is a classic cause of grounding, and "
                        + "the reason the first thing you check on an unfamiliar coast is the region."));

        bank.add(new Question(
                "A light charted as Fl(3) 20s shows",
                new String[]{"A fixed light for 20 seconds", "Three occultations per minute",
                        "Three flashes every 20 seconds", "Three separate lights"},
                2,
                "The bracket is the group count; the number after it is the period of one full "
                        + "cycle. Fl(3) 20s is three flashes, then darkness, repeating every twenty "
                        + "seconds — you time the cycle, not the flash."));

        bank.add(new Question(
                "A sounding of 12 on a metric chart means",
                new String[]{"12 fathoms below the keel", "12 metres of clearance under the keel",
                        "12 metres at mean high water", "12 metres of water below chart datum"},
                3,
                "Soundings are depths below chart datum, not below your keel. Subtract your draft, "
                        + "then add the height of tide, before deciding the water is safe."));

        bank.add(new Question(
                "Mercator's projection keeps compass bearings straight but distorts",
                new String[]{"The compass rose", "Scale, increasingly with latitude", "Depth",
                        "Magnetic variation"},
                1,
                "That is why Greenland looks the size of Africa. The trade is deliberate: a "
                        + "straight line on a Mercator chart is a constant bearing on the water, "
                        + "which is exactly what a hand-steered course needs."));

        bank.add(new Question(
                "Dead reckoning is a position found from",
                new String[]{"Course, speed and time since the last fix", "The last satellite fix",
                        "A line of soundings", "The height of the tide"},
                0,
                "It accumulates error with every course change and every current you did not "
                        + "allow for, so it is corrected by a fix as soon as one is available."));

        bank.add(new Question(
                "Facing forward on a vessel, port and starboard are",
                new String[]{"Port right, starboard left", "Port aft, starboard forward",
                        "Port forward, starboard aft", "Port left, starboard right"},
                3,
                "Port is left, starboard is right, and both are fixed to the vessel rather than "
                        + "to the person on deck — the reason an order is repeated back before it "
                        + "is carried out."));

        return Collections.unmodifiableList(bank);
    }
}
