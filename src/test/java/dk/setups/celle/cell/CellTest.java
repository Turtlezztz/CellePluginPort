package dk.setups.celle.cell;

import dk.setups.celle.config.DefaultConfig;
import org.junit.jupiter.api.*;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CellTest {
    CellGroup group;
    Cell cell;
    @BeforeEach void setup() {
        new DefaultConfig();
        group = new CellGroup("a");
        group.setRentTimeMillis(1_000L);
        group.setMaxRentTimeMillis(5_000L);
        cell = new Cell("test", group, new CellRegion("test", "world"));
    }
    @Test void expiredAndUnownedCellsCannotBeExtended() {
        assertFalse(cell.canExtend());
        assertEquals(0, cell.getTimeLeftMs());
        cell.setRentedUntil(new Date(System.currentTimeMillis() + 1_000));
        assertFalse(cell.isRented());
        cell.setOwner(new CellUser(UUID.randomUUID(), "owner"));
        cell.setRentedUntil(new Date(0));
        assertFalse(cell.canExtend());
        assertThrows(IllegalStateException.class, cell::extend);
    }
    @Test void extensionRespectsMaximumRentalWindow() {
        cell.setOwner(new CellUser(UUID.randomUUID(), "owner"));
        cell.setRentedUntil(new Date(System.currentTimeMillis() + 10_000));
        assertFalse(cell.canExtend());
        cell.setRentedUntil(new Date(System.currentTimeMillis() + 1_000));
        assertTrue(cell.canExtend());
        long before = cell.getRentedUntil().getTime();
        cell.extend();
        assertEquals(before + 1_000, cell.getRentedUntil().getTime());
    }
    @Test void firstAndFourthSignLinesUseHumanNumbering() {
        group.setUnrentedSignLine(1, "first");
        group.setUnrentedSignLine(4, "last");
        assertEquals("first", group.getUnrentedSignLines().get(0));
        assertEquals("last", group.getUnrentedSignLines().get(3));
        assertNotEquals("first", DefaultConfig.getInstance().getUnrentedSignLines().get(0));
    }
}
