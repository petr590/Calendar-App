package com.x590.calendar;

import androidx.room.TypeConverter;
import lombok.experimental.UtilityClass;

import java.util.Calendar;

@UtilityClass
public final class CalendarConverter {
    @TypeConverter
    public static Long fromCalendar(Calendar calendar) {
        return calendar == null ? null : calendar.getTimeInMillis();
    }

    @TypeConverter
    public static Calendar toCalendar(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }
}