package com.x590.calendar.database;

import androidx.room.TypeConverter;
import com.x590.calendar.R;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Calendar;

@Getter
@RequiredArgsConstructor
public enum PeriodUnit {
	MINUTES (Calendar.MINUTE,       R.plurals.every_n_minutes),
	HOURS   (Calendar.HOUR_OF_DAY,  R.plurals.every_n_hours),
	DAYS    (Calendar.DAY_OF_MONTH, R.plurals.every_n_days),
	WEEKS   (Calendar.WEEK_OF_YEAR, R.plurals.every_n_weeks),
	MONTHS  (Calendar.MONTH,        R.plurals.every_n_months),
	YEARS   (Calendar.YEAR,         R.plurals.every_n_years);

	private final int type;

	private final int pluralId;

	public int position() {
		return ordinal();
	}


	private static final PeriodUnit[] VALUES = values();

	public static PeriodUnit byPosition(int position) {
		if (position < 0 || position >= VALUES.length) {
			throw new IllegalArgumentException("Position: " + position);
		}

		return VALUES[position];
	}

	@UtilityClass
	public static final class Converter {
		@TypeConverter
		public static Integer fromPeriodUnit(PeriodUnit unit) {
			return unit == null ? null : unit.ordinal();
		}

		@TypeConverter
		public static PeriodUnit toPeriodUnit(Integer ordinal) {
			return ordinal == null ? null : VALUES[ordinal];
		}
	}
}