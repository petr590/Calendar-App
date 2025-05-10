package com.x590.calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.x590.calendar.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	
	private final int[] monthStringIds = {
			R.string.january,
			R.string.february,
			R.string.march,
			R.string.april,
			R.string.may,
			R.string.june,
			R.string.july,
			R.string.august,
			R.string.september,
			R.string.october,
			R.string.november,
			R.string.december,
	};

	private final List<Button> buttons = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		Calendar now = Calendar.getInstance();
		now.setLenient(false);
		binding.yearMonth.setText(formatYearMonth(now));

		Calendar date = (Calendar) now.clone();
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.add(Calendar.DAY_OF_WEEK, getDaysOffset(date));
		assert date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;

		initButtons(date, now.get(Calendar.MONTH));
	}

	private void initButtons(Calendar date, final int currentMonth) {
		for (int i = 0; i < binding.calendar.getChildCount(); i++) {
			View child1 = binding.calendar.getChildAt(i);
			if (!(child1 instanceof TableRow)) continue;
			TableRow row = (TableRow) child1;

			for (int j = 0; j < row.getChildCount(); j++) {
				View child2 = row.getChildAt(j);

				if ("day".equals(child2.getTag()) && child2 instanceof Button) {
					Button button = (Button) child2;
					button.setOnClickListener(this::onDayClick);
					button.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));

					if (date.get(Calendar.MONTH) != currentMonth) {
						button.setTextAppearance(R.style.Theme_CalendarApp_OtherMonthCell);
					}

					date.add(Calendar.DAY_OF_MONTH, 1);
					buttons.add(button);

				} else if ("weekNum".equals(child2.getTag()) && child2 instanceof TextView) {
					TextView textView = (TextView) child2;
					textView.setText(String.valueOf(date.get(Calendar.WEEK_OF_YEAR)));
				}
			}
		}
	}

	/** @return Смещение в днях до предыдущего понедельника - число от -6 до 0. */
	private static int getDaysOffset(Calendar day) {
		switch (day.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:    return  0;
			case Calendar.TUESDAY:   return -1;
			case Calendar.WEDNESDAY: return -2;
			case Calendar.THURSDAY:  return -3;
			case Calendar.FRIDAY:    return -4;
			case Calendar.SATURDAY:  return -5;
			case Calendar.SUNDAY:    return -6;
			default: throw new IllegalStateException("Illegal day of week: " + day.get(Calendar.DAY_OF_WEEK));
		}
	}

	private void onDayClick(View button) {
		// TODO
	}
	
	private String formatYearMonth(Calendar yearMonth) {
		int year = yearMonth.get(Calendar.YEAR);
		String month = getResources().getString(monthStringIds[yearMonth.get(Calendar.MONTH)]);
		return year + " " + month;
	}
}