package de.jochen_manns.buyitv0;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateOnly {
    private static final Pattern DateRegEx = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");

    private LocalDate _date;

    private DateOnly(LocalDate date) {
        _date = date;
    }

    public static DateOnly parse(String value) {
        if (value == null || value.isEmpty()) return null;

        Matcher matcher = DateRegEx.matcher(value);

        if (!matcher.find()) return null;

        return new DateOnly(LocalDate.of(
                Integer.parseInt(matcher.group(1), 10),
                Integer.parseInt(matcher.group(2), 10),
                Integer.parseInt(matcher.group(3), 10)));
    }

    public boolean isFuture() {
        return _date.isAfter(LocalDate.now());
    }

    public boolean isPast() {
        return _date.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return format(_date.getYear(), _date.getMonthValue(), _date.getDayOfMonth());
    }

    public static String format(int year, int month, int day) {
        return MessageFormat.format("{0,number,0000}-{1,number,00}-{2,number,00}", year, month, day);
    }

    public int getYear() {
        return _date.getYear();
    }

    public int getMonth() {
        return _date.getMonthValue();
    }

    public int getDay() {
        return _date.getDayOfMonth();
    }
}
