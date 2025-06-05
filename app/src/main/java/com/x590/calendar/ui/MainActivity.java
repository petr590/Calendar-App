package com.x590.calendar.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.x590.calendar.CalendarApp;
import com.x590.calendar.R;
import com.x590.calendar.database.Task;
import com.x590.calendar.Util;
import com.x590.calendar.databinding.ActivityMainBinding;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
	public static final String TASK_ID_EXTRA = "taskId";

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

	@Getter(AccessLevel.PACKAGE)
	private ActivityMainBinding binding;

	private DateFormat dateTimeFormat;

	private ViewPager2.OnPageChangeCallback onPageChangeCallback;

	// ------------------------------------------ create ------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dateTimeFormat = Util.getDateTimeFormat(getResources().getConfiguration().getLocales().get(0));

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());


		val adapter = new CalendarPagerAdapter(this::loadTasks);
		onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				binding.calendarTitle.setText(formatYearMonth(adapter.getCurrentMonthByPos(position)));
			}
		};

		binding.calendarPager.setAdapter(adapter);
		binding.calendarPager.registerOnPageChangeCallback(onPageChangeCallback);
		binding.calendarPager.setCurrentItem(CalendarPagerAdapter.START_INDEX, false);

		binding.addTaskButton.setOnClickListener(view -> addTask());

		initTasksContainer();

		Intent intent = getIntent();
		if (intent == null) return;

		int id = intent.getIntExtra(TASK_ID_EXTRA, 0);
		if (id == 0) return;

		CalendarApp.databaseRequest(
				database -> database.taskDao().getById(id),
				task -> {
					Calendar initialDate = adapter.getInitialDate();
					Calendar taskDate = task.getTimestamp();
					binding.calendarPager.setCurrentItem(
							CalendarPagerAdapter.START_INDEX + Util.monthNumber(taskDate) - Util.monthNumber(initialDate)
					);
				}
		);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		binding.calendarPager.unregisterOnPageChangeCallback(onPageChangeCallback);
	}

	/** Инициализирует {@link ActivityMainBinding#tasksContainer} */
	private void initTasksContainer() {
		binding.tasksContainer.setLayoutManager(new LinearLayoutManager(
				binding.tasksContainer.getContext(), LinearLayoutManager.VERTICAL, false
		));

		val divider = new DividerItemDecoration(
				binding.tasksContainer.getContext(), DividerItemDecoration.VERTICAL
		);

		Drawable shape = ContextCompat.getDrawable(this, R.drawable.shape_task_divider);
		divider.setDrawable(Objects.requireNonNull(shape));
		binding.tasksContainer.addItemDecoration(divider);

		binding.taskTimePicker.setIs24HourView(true);
		binding.saveTaskButton.setOnClickListener(view -> saveTask());
		binding.cancelTaskEditButton.setOnClickListener(view -> hideTaskEditPanel());
	}

	private void loadTasks(Calendar date) {
		CalendarApp.databaseRequest(
				database -> database.taskDao().getByDay(date),
				tasks -> {
					val adapter = new TaskAdapter(tasks);
					updateNoTasksText(adapter);

					adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
						public void onChanged() {
							updateNoTasksText(adapter);
						}

						public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
							updateNoTasksText(adapter);
						}

						public void onItemRangeInserted(int positionStart, int itemCount) {
							updateNoTasksText(adapter);
						}

						public void onItemRangeRemoved(int positionStart, int itemCount) {
							updateNoTasksText(adapter);
						}

						public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
							updateNoTasksText(adapter);
						}
					});

					binding.tasksContainer.setAdapter(adapter);
				}
		);
	}

	// ---------------------------------------- task edit -----------------------------------------
	private @Nullable Task editingTask;
	private int editingTaskPosition = -1;

	/**
	 * Показывает {@link ActivityMainBinding#overlay} и {@link ActivityMainBinding#taskEditPanel},
	 * задаёт данные из task в соответствующие элементы.
	 */
	public void showTaskEditPanel(Task task, int taskPosition) {
		ActivityMainBinding binding = this.binding;

		binding.overlay.setVisibility(View.VISIBLE);
		binding.taskEditPanel.setVisibility(View.VISIBLE);

		binding.taskDescription.setText(task.getDescription());
		binding.taskReminderEnabled.setChecked(task.isReminderEnabled());
		binding.taskTimePicker.setHour(task.getTimestamp().get(Calendar.HOUR_OF_DAY));
		binding.taskTimePicker.setMinute(task.getTimestamp().get(Calendar.MINUTE));

		editingTask = task;
		editingTaskPosition = taskPosition;

		binding.taskDescription.requestFocus();
		binding.taskDescription.setSelection(binding.taskDescription.getText().length());

		InputMethodManager manager = getSystemService(InputMethodManager.class);
		manager.showSoftInput(binding.taskDescription, InputMethodManager.SHOW_IMPLICIT);
	}

	private void addTask() {
		val adapter = (CalendarPagerAdapter) binding.calendarPager.getAdapter();
		if (adapter == null) return;

		Calendar day = adapter.getCurrentDay();
		Calendar timestamp = Calendar.getInstance();
		timestamp.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));

		showTaskEditPanel(new Task(timestamp), -1);
	}

	private void hideTaskEditPanel() {
		View view = getCurrentFocus();
		if (view != null) {
			InputMethodManager manager = getSystemService(InputMethodManager.class);
			manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}

		binding.overlay.setVisibility(View.GONE);
		binding.taskEditPanel.setVisibility(View.GONE);

		editingTask = null;
		editingTaskPosition = -1;
	}

	private void saveTask() {
		final Task task = editingTask;

		if (task != null) {
			task.setDescription(binding.taskDescription.getText().toString());
			task.setReminderEnabled(binding.taskReminderEnabled.isChecked());
			task.getTimestamp().set(Calendar.HOUR_OF_DAY, binding.taskTimePicker.getHour());
			task.getTimestamp().set(Calendar.MINUTE, binding.taskTimePicker.getMinute());

			if (!task.isValid()) return;

			TaskAdapter adapter = (TaskAdapter) binding.tasksContainer.getAdapter();

			if (adapter != null) {
				if (editingTaskPosition >= 0) {
					adapter.notifyItemChanged(editingTaskPosition);
				} else {
					adapter.addTask(task);
				}
			}

			CalendarApp.databaseRequest(
					database -> {
						if (task.getId() != 0) {
							database.taskDao().update(task);
						} else {
							task.setId(database.taskDao().insert(task));
						}
					},

					() -> updateNotification(task)
			);
		}

		hideTaskEditPanel();
	}

	private void updateNoTasksText(TaskAdapter adapter) {
		binding.noTasks.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
	}


	// ------------------------------------------ alarm -------------------------------------------

	void updateNotification(Task task) {
		if (task.needNotification()) {
			addNotification(task);
		} else {
			removeNotification(task);
		}
	}

	void addNotification(Task task) {
		val alarmManager = getSystemService(AlarmManager.class);

		alarmManager.setExactAndAllowWhileIdle(
				AlarmManager.RTC_WAKEUP,
				task.getTimestamp().getTimeInMillis(),
				getBroadcastPendingIntent(task)
		);

		String dateTime = dateTimeFormat.format(task.getTimestamp().getTime());
		Toast.makeText(this, getString(R.string.reminder_set_for, dateTime), Toast.LENGTH_LONG).show();
	}

	void removeNotification(Task task) {
		val alarmManager = getSystemService(AlarmManager.class);
		alarmManager.cancel(getBroadcastPendingIntent(task));

		String dateTime = dateTimeFormat.format(task.getTimestamp().getTime());
		Toast.makeText(this, String.format("Removed: %s", dateTime), Toast.LENGTH_SHORT).show();
	}

	private PendingIntent getBroadcastPendingIntent(Task task) {
		assert task.getId() != 0;

		val intent = new Intent(this, ReminderNotificationReceiver.class)
				.setData(Uri.parse("calendar-app://task/" + task.getId()))
				.putExtra(TASK_ID_EXTRA, task.getId());

		return PendingIntent.getBroadcast(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
	}

	// ------------------------------------------ utils -------------------------------------------
	private String formatYearMonth(Calendar yearMonth) {
		int year = yearMonth.get(Calendar.YEAR);
		String month = getResources().getString(monthStringIds[yearMonth.get(Calendar.MONTH)]);
		return year + " " + month;
	}
}