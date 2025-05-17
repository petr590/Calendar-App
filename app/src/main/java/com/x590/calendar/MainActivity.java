package com.x590.calendar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.x590.calendar.databinding.ActivityMainBinding;

import java.util.Calendar;
import java.util.Objects;

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

	// --------------------------------------------- create ---------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		binding.calendarTitle.setText(formatYearMonth(today()));
		initCalendar();
		initTasksContainer();

		Task task = new Task();
		task.setDescription("абоба x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x");
		task.setDate(Calendar.getInstance());
		task.setReminderEnabled(false);
		task.setFinished(true);
		CalendarApp.databaseRequest(db -> db.taskDao().insert(task));

		loadTasks(today());
	}

	private void initCalendar() {
		Calendar today = today();
		Calendar date = today();
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.add(Calendar.DAY_OF_WEEK, getDaysOffset(date));
		assert date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;

		if (date.get(Calendar.DAY_OF_MONTH) == 1 && date.getActualMaximum(Calendar.DAY_OF_MONTH) == 28) {
			binding.lastRow.setVisibility(View.GONE);
		}

		for (int i = 0; i < binding.calendar.getChildCount(); i++) {
			View child1 = binding.calendar.getChildAt(i);
			if (!(child1 instanceof TableRow) || child1.getVisibility() == View.GONE) continue;
			TableRow row = (TableRow) child1;

			for (int j = 0; j < row.getChildCount(); j++) {
				View child2 = row.getChildAt(j);

				if ("day".equals(child2.getTag()) && child2 instanceof Button) {
					Button button = (Button) child2;
					button.setOnClickListener(this::onDayClick);
					button.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
					button.setTag(R.id.tag_date, date.clone());

					if (date.get(Calendar.MONTH) != today.get(Calendar.MONTH)) {
						button.setTextAppearance(R.style.Theme_CalendarApp_OtherMonthCell);

					} else if (date.equals(today)) {
						button.setTextAppearance(R.style.Theme_CalendarApp_TodayCell);
					}

					date.add(Calendar.DAY_OF_MONTH, 1);

				} else if ("weekNum".equals(child2.getTag()) && child2 instanceof TextView) {
					TextView textView = (TextView) child2;
					textView.setText(String.valueOf(date.get(Calendar.WEEK_OF_YEAR)));
				}
			}
		}
	}

	private void initTasksContainer() {
		binding.tasksContainer.setLayoutManager(new LinearLayoutManager(
				binding.tasksContainer.getContext(), LinearLayoutManager.VERTICAL, false
		));

		DividerItemDecoration divider = new DividerItemDecoration(
				binding.tasksContainer.getContext(), DividerItemDecoration.VERTICAL
		);

		Drawable shape = ContextCompat.getDrawable(this, R.drawable.shape_task_divider);
		divider.setDrawable(Objects.requireNonNull(shape));
		binding.tasksContainer.addItemDecoration(divider);
	}

	// --------------------------------------------- main ---------------------------------------------

	private void onDayClick(View button) {
		Calendar date = (Calendar) button.getTag(R.id.tag_date);
		loadTasks(date);
	}

	private void loadTasks(Calendar date) {
		CalendarApp.databaseRequest(this,
				database -> database.taskDao().getByDay(date),
				tasks -> binding.tasksContainer.setAdapter(new TaskAdapter(tasks))
		);
	}

	// --------------------------------------------- utils ---------------------------------------------

	private String formatYearMonth(Calendar yearMonth) {
		int year = yearMonth.get(Calendar.YEAR);
		String month = getResources().getString(monthStringIds[yearMonth.get(Calendar.MONTH)]);
		return year + " " + month;
	}

	private static Calendar today() {
		Calendar today = Calendar.getInstance();
		today.setLenient(false);
		return today;
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
			default: throw new IllegalStateException("Invalid day of week: " + day.get(Calendar.DAY_OF_WEEK));
		}
	}
}