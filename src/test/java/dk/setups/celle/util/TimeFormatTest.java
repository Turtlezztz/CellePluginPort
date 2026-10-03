package dk.setups.celle.util;
import dk.setups.celle.config.Config;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class TimeFormatTest {
    TimeFormat format;
    @BeforeEach void setup() throws Exception {
        format = new TimeFormat();
        var field = TimeFormat.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(format,new Config());
    }
    @Test void zeroAndExpiredTimesDoNotCrashGuiRendering() {
        assertEquals("0 seconds", format.formatLongLeft(0));
        assertEquals("0 seconds", format.formatLongLeft(-1000));
        assertEquals("0s", format.formatConsiseLeft(0));
    }
    @Test void displaysLargerUnitsFirst() {
        assertEquals("1d 2h 3m", format.formatConsiseLeft(86_400_000 + 7_200_000 + 180_000));
        assertEquals("1 hour", format.formatLongLeft(3_600_000));
    }
}
