package dk.setups.celle.command.types;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
class DurationTypeResolverTest {
    @Test void parsesWholeDurationWithDanishHourAlias() {
        assertEquals(Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(5), DurationTypeResolver.parse("1d2t30m5s"));
    }
    @Test void rejectsIncompleteInvalidZeroAndOverflowingDurations() {
        for (String invalid : new String[]{"", "1", "2x", "1d2", "-1h", "0s", "1hjunk", "999999999999999999999d"})
            assertThrows(IllegalArgumentException.class, () -> DurationTypeResolver.parse(invalid), invalid);
    }
}
