package com.x590.calendar;

import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarApiTest {
	@Test
	public void testCalendarApi() {
		Calendar calendar = Calendar.getInstance();
		System.out.println(calendar.get(Calendar.DAY_OF_WEEK));

		DateFormat format = new SimpleDateFormat("dd MMMM yyyy, EEEE");

		calendar.set(Calendar.DAY_OF_MONTH, 1);
		System.out.println(format.format(calendar.getTime()));

		calendar.add(Calendar.DAY_OF_WEEK, -1);
		System.out.println(format.format(calendar.getTime()));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDateFormat() {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");

		System.out.println(timeFormat.format(new Date(2020, 9, 15,  3, 32, 15)));

		timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

		System.out.println(timeFormat.format(new Date(2020, 9, 15, 22, 30, 12)));
		System.out.println(timeFormat.format(new Date(2020, 9, 15,  3, 32, 15)));
	}

	@Test
	public void testDiapason() {
		Calendar day = Calendar.getInstance();

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		int year = day.get(Calendar.YEAR);
		int month = day.get(Calendar.MONTH);
		int day_of_month = day.get(Calendar.DAY_OF_MONTH);

		from.set(year, month, day_of_month, 0, 0, 0);
		to.set(year, month, day_of_month + 1, 0, 0, 0);

		from.set(Calendar.MILLISECOND, 0);
		to.set(Calendar.MILLISECOND, 0);

		Assert.assertEquals(0, from.get(Calendar.HOUR));
		Assert.assertEquals(0, from.get(Calendar.MINUTE));
		Assert.assertEquals(0, from.get(Calendar.SECOND));
		Assert.assertEquals(0, from.get(Calendar.MILLISECOND));

		Assert.assertEquals(0, to.get(Calendar.HOUR));
		Assert.assertEquals(0, to.get(Calendar.MINUTE));
		Assert.assertEquals(0, to.get(Calendar.SECOND));
		Assert.assertEquals(0, to.get(Calendar.MILLISECOND));
	}

	@Test
	public void testDateTimeFormat() {
		DateFormat format = new SimpleDateFormat("dd MMMM HH:mm", Locale.ENGLISH);
		System.out.println(format.format(Calendar.getInstance().getTime()));
	}
}