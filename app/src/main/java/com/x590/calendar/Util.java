package com.x590.calendar;

import lombok.experimental.UtilityClass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@UtilityClass
public class Util {
	private static final DateFormat DATETIME_FORMAT_RU = new SimpleDateFormat("d\u00A0MMMM\u00A0HH:mm", Locale.forLanguageTag("ru"));
	private static final DateFormat DATETIME_FORMAT_EN = new SimpleDateFormat("MMMM\u00A0d,\u00A0HH:mm", Locale.ENGLISH);

	/** @return Формат с указанием числа, месяца, часов и минут для системной локали. */
	public static DateFormat getDateTimeFormat(Locale locale) {
		if (locale.getLanguage().equals("ru"))
			return DATETIME_FORMAT_RU;

		return DATETIME_FORMAT_EN;
	}


	/** @return Смещение в днях до предыдущего понедельника - число от -6 до 0. */
	public static int getDaysOffset(Calendar day) {
		switch (day.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:    return  0;
			case Calendar.TUESDAY:   return -1;
			case Calendar.WEDNESDAY: return -2;
			case Calendar.THURSDAY:  return -3;
			case Calendar.FRIDAY:    return -4;
			case Calendar.SATURDAY:  return -5;
			case Calendar.SUNDAY:    return -6;
			default: throw new IllegalStateException("Invalid day of week: " + day.get(Calendar.DAY_OF_WEEK));
		}
	}

	/**
	 * @return Экземпляр класса календарь с переданной датой и
	 * часами, минутами, секундами и миллисекундами, установленными в ноль.
	 */
	public static Calendar getDay(int year, int month, int day) {
		Calendar date = Calendar.getInstance();
		date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}

	/** @return Номер месяца, начиная с нулевого года */
	public static int monthNumber(Calendar yearMonth) {
		return yearMonth.get(Calendar.YEAR) * 12 + yearMonth.get(Calendar.MONTH);
	}
}