package dk.setups.celle.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration(path = "config.yml", provider = YamlSnakeYamlConfigurer.class)
@Header({
        "Configure the plugin settings here.",
        "Run /cea reload after editing this file to apply your changes.",
        "",
        "Unicode characters are supported."
})
public class Config extends OkaeriConfig {

        @Comment("Maximum cells per player. Permissions are checked in order; the first matching permission determines the limit.")
    private Map<String, Integer> maxCellsPerPlayer = getDefaultMaxCellsPerPlayer();
    private Map<String, Integer> getDefaultMaxCellsPerPlayer() {
        Map<String, Integer> maxCellsPerPlayer = new LinkedHashMap<>();
        maxCellsPerPlayer.put("donator", 2);
        maxCellsPerPlayer.put("default", 1);
        return maxCellsPerPlayer;
    }

    @Comment({
            "Time format configuration",
            "Customize the time units used in messages. Each list contains the singular and plural labels. Supported keys:",
            " * SECOND",
            " * MINUTE",
            " * HOUR",
            " * DAY",
            " * MONTH",
            " * YEAR"
    })

    private Map<TimeKey, List<String>> timeFormatConcise = getDefaultTimeFormatConcise();
    private Map<TimeKey, List<String>> getDefaultTimeFormatConcise() {
        Map<TimeKey, List<String>> map = new LinkedHashMap<>();
        map.put(TimeKey.SECOND, Arrays.asList("s", "s"));
        map.put(TimeKey.MINUTE, Arrays.asList("m", "m"));
        map.put(TimeKey.HOUR, Arrays.asList("h", "h"));
        map.put(TimeKey.DAY, Arrays.asList("d", "d"));
        map.put(TimeKey.MONTH, Arrays.asList("mo", "mo"));
        map.put(TimeKey.YEAR, Arrays.asList("y", "y"));
        return map;
    }
    @Comment({"Separator between concise time units, for example 1d 2h 3m. Use a space to separate the units.",
             "Each time-unit list uses the singular label first, then the plural label."
    })
    private String timeFormatConciseSeparator = " ";
    private Map<TimeKey, List<String>> timeFormatLong = getDefaultTimeFormatLong();
    @Comment("Separator between a number and its time unit, for example 1 day.")
    private String timeFormatLongSeparator = " ";
    private Map<TimeKey, List<String>> getDefaultTimeFormatLong() {
        Map<TimeKey, List<String>> map = new LinkedHashMap<>();
        map.put(TimeKey.SECOND, Arrays.asList("second", "seconds"));
        map.put(TimeKey.MINUTE, Arrays.asList("minute", "minutes"));
        map.put(TimeKey.HOUR, Arrays.asList("hour", "hours"));
        map.put(TimeKey.DAY, Arrays.asList("day", "days"));
        map.put(TimeKey.MONTH, Arrays.asList("month", "months"));
        map.put(TimeKey.YEAR, Arrays.asList("year", "years"));
        return map;
    }

    @Comment("Uses Java DateTimeFormatter patterns. See https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/format/DateTimeFormatter.html")
    private String dateFormatShort = "dd/MM, HH:mm";
    @Comment("Replaces {day}, {day-number}, {month}, {year}, {hour} and {minute}. Customize month and weekday names below.")
    private String dateFormatLong = "{day}, {day-number} {month} {year}, {hour}:{minute}";
    private Map<Month, String> dateFormatLongMonthNames = getDefaultMonthNames();
    private Map<Month, String> getDefaultMonthNames() {
        Map<Month, String> map = new LinkedHashMap<>();
        map.put(Month.JANUARY, "January");
        map.put(Month.FEBRUARY, "February");
        map.put(Month.MARCH, "March");
        map.put(Month.APRIL, "April");
        map.put(Month.MAY, "May");
        map.put(Month.JUNE, "June");
        map.put(Month.JULY, "July");
        map.put(Month.AUGUST, "August");
        map.put(Month.SEPTEMBER, "September");
        map.put(Month.OCTOBER, "October");
        map.put(Month.NOVEMBER, "November");
        map.put(Month.DECEMBER, "December");
        return map;
    }
    private Map<DayOfWeek, String> dateFormatLongDayNames = getDefaultDayNames();
    private Map<DayOfWeek, String> getDefaultDayNames() {
        Map<DayOfWeek, String> map = new HashMap<>();
        map.put(DayOfWeek.MONDAY, "Monday");
        map.put(DayOfWeek.TUESDAY, "Tuesday");
        map.put(DayOfWeek.WEDNESDAY, "Wednesday");
        map.put(DayOfWeek.THURSDAY, "Thursday");
        map.put(DayOfWeek.FRIDAY, "Friday");
        map.put(DayOfWeek.SATURDAY, "Saturday");
        map.put(DayOfWeek.SUNDAY, "Sunday");
        return map;
    }


    public enum TimeKey {
        SECOND(1_000),
        MINUTE(60_000),
        HOUR(3_600_000),
        DAY(86_400_000),
        MONTH(2_592_000_000L),
        YEAR(31_536_000_000L);

        @Getter
        private final long ms;

        TimeKey(long ms) {
            this.ms = ms;
        }
    }

    @Comment({
            "Interval between sign updates.",
            "Measured in ticks: 20 ticks equal 1 second.",
            "",
            "Signs and expired rentals are updated on the server thread.",
            "Also controls how often expired rentals are checked."
    })
    private int updateSignsDelayTick = 20;

    @Comment({
            "Cooldown for interactions such as clicking signs. Does not affect commands.",
            "Increase this to limit repeated sign interactions from autoclickers.",
            "Measured in milliseconds: 1000 milliseconds equal 1 second."
    })
    private int cooldownMs = 250;

    private boolean ironDoorOpen = true;
    private boolean teleportOutOfCell = true;
}
