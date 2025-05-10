package com.x590.calendar;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
}