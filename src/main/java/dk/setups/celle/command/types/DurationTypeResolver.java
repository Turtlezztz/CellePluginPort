package dk.setups.celle.command.types;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import java.time.Duration;
import java.util.regex.Pattern;

public class DurationTypeResolver extends BasicTypeResolver<Duration> {
    private static final Pattern PART = Pattern.compile("(\\d+)([dhmts])", Pattern.CASE_INSENSITIVE);
    @Override public boolean supports(@NonNull Class<?> type) { return type.equals(Duration.class); }
    @Override public Duration resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argument, @NonNull String text) {
        return parse(text);
    }
    public static Duration parse(String text) {
        var matcher = PART.matcher(text);
        Duration duration = Duration.ZERO;
        int end = 0;
        try {
            while (matcher.find()) {
                if (matcher.start() != end) throw new IllegalArgumentException("Invalid duration: " + text);
                long amount = Long.parseLong(matcher.group(1));
                duration = switch (Character.toLowerCase(matcher.group(2).charAt(0))) {
                    case 'd' -> duration.plusDays(amount);
                    case 'h', 't' -> duration.plusHours(amount);
                    case 'm' -> duration.plusMinutes(amount);
                    case 's' -> duration.plusSeconds(amount);
                    default -> throw new IllegalArgumentException("Invalid duration unit");
                };
                end = matcher.end();
            }
            if (end != text.length() || duration.isZero()) throw new IllegalArgumentException("Use a positive duration such as 1d2h30m");
            duration.toMillis();
            return duration;
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Duration is too large", exception);
        }
    }
}
