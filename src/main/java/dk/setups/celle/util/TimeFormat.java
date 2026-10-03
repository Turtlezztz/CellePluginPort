package dk.setups.celle.util;

import dk.setups.celle.config.Config;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.core.annotation.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class TimeFormat {
    private @Inject Config config;

    public String formatConsiseLeft(Date date) { return formatConsiseLeft(date == null ? 0 : date.getTime() - System.currentTimeMillis()); }
    public String formatConsiseLeft(long millisLeft) {
        Map<Config.TimeKey, List<String>> labels = config.getTimeFormatConcise();
        long remaining = Math.max(0, millisLeft);
        List<String> parts = new ArrayList<>();
        for (Config.TimeKey unit : labels.keySet().stream().sorted(Comparator.comparingLong(Config.TimeKey::getMs).reversed()).toList()) {
            long value = remaining / unit.getMs();
            if (value > 0) {
                parts.add(value + label(labels.get(unit), value));
                remaining %= unit.getMs();
            }
        }
        return parts.isEmpty() ? "0" + label(labels.get(Config.TimeKey.SECOND), 0) : String.join(config.getTimeFormatConciseSeparator(), parts);
    }
    public String formatLongLeft(Date date) { return formatLongLeft(date == null ? 0 : date.getTime() - System.currentTimeMillis()); }
    public String formatLongLeft(long millisLeft) {
        Map<Config.TimeKey, List<String>> labels = config.getTimeFormatLong();
        long remaining = Math.max(0, millisLeft);
        Config.TimeKey unit = labels.keySet().stream().filter(key -> remaining >= key.getMs())
                .max(Comparator.comparingLong(Config.TimeKey::getMs)).orElse(Config.TimeKey.SECOND);
        long value = remaining / unit.getMs();
        return value + config.getTimeFormatLongSeparator() + label(labels.get(unit), value);
    }
    private String label(List<String> labels, long value) {
        if (labels == null || labels.isEmpty()) return "";
        return labels.get(value == 1 || labels.size() == 1 ? 0 : 1);
    }
    public String formatDateShort(Date date) {
        return DateTimeFormatter.ofPattern(config.getDateFormatShort()).format(local(date));
    }
    public String formatDateLong(Date date) {
        ZonedDateTime time = local(date);
        return PlaceholderContext.of(CompiledMessage.of(config.getDateFormatLong()))
                .with("day-number", two(time.getDayOfMonth())).with("month-number", two(time.getMonthValue()))
                .with("year", time.getYear()).with("hour", two(time.getHour())).with("minute", two(time.getMinute()))
                .with("second", two(time.getSecond())).with("day", config.getDateFormatLongDayNames().get(time.getDayOfWeek()))
                .with("month", formatMonth(time.getMonthValue())).apply();
    }
    private ZonedDateTime local(Date date) { return date.toInstant().atZone(ZoneId.systemDefault()); }
    private String two(int value) { return String.format(Locale.ROOT, "%02d", value); }
    public String formatDay(Date date) { return config.getDateFormatLongDayNames().get(local(date).getDayOfWeek()); }
    public String formatMonth(int month) { return config.getDateFormatLongMonthNames().get(Month.of(month)); }
}
