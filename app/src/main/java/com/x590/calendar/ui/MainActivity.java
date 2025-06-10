package com.x590.calendar.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.x590.calendar.CalendarApp;
import com.x590.calendar.R;
import com.x590.calendar.Util;
import com.x590.calendar.database.Task;
import com.x590.calendar.databinding.ActivityMainBinding;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Nullable;

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

	private ViewPager2.OnPageChangeCallback onPageChangeCallback;

	// ------------------------------------------ create ------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initCalendarPager();
		initTasksContainer();

		binding.addTaskButton.setOnClickListener(view -> addTask());
	}

	/** Инициализирует {@link ActivityMainBinding#calendarPager} */
	private void initCalendarPager() {
		val adapter = new CalendarPagerAdapter(this::loadTasks);

		onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				binding.calendarTitle.setText(formatYearMonth(adapter.getMonthByPos(position)));
			}
		};

		binding.calendarPager.setAdapter(adapter);
		binding.calendarPager.registerOnPageChangeCallback(onPageChangeCallback);
		binding.calendarPager.setCurrentItem(CalendarPagerAdapter.START_INDEX, false);


		if (getIntent() == null) return;
		int id = getIntent().getIntExtra(TASK_ID_EXTRA, 0);
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

	/** Инициализирует {@link ActivityMainBinding#tasksContainer} */
	private void initTasksContainer() {
		binding.tasksContainer.setLayoutManager(new LinearLayoutManager(
				binding.tasksContainer.getContext(), LinearLayoutManager.VERTICAL, false
		));

		val divider = new DividerItemDecoration(
				binding.tasksContainer.getContext(), DividerItemDecoration.VERTICAL
		);

		val shape = ContextCompat.getDrawable(this, R.drawable.shape_task_divider);
		divider.setDrawable(Objects.requireNonNull(shape));
		binding.tasksContainer.addItemDecoration(divider);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		binding.calendarPager.unregisterOnPageChangeCallback(onPageChangeCallback);
	}

	// ---------------------------------------- task edit -----------------------------------------

	/**
	 * Показывает {@link ActivityMainBinding#overlay} и {@link ActivityMainBinding#taskEditPanel},
	 * задаёт данные из task в соответствующие элементы.
	 */
	public void showTaskEditPanel(Task task, int taskPosition) {
		binding.overlay.setVisibility(View.VISIBLE);
		binding.taskEditPanel.setVisibility(View.VISIBLE);
		binding.taskEditPanel.<TaskEditPanelFragment>getFragment().show(task, taskPosition);
	}

	/** Скрывает {@link ActivityMainBinding#overlay} и {@link ActivityMainBinding#taskEditPanel} */
	public void hideTaskEditPanel() {
		binding.overlay.setVisibility(View.GONE);
		binding.taskEditPanel.setVisibility(View.GONE);
	}

	private void addTask() {
		val adapter = (CalendarPagerAdapter) binding.calendarPager.getAdapter();
		if (adapter == null) return;

		Calendar day = adapter.getCurrentDay();
		Calendar timestamp = Calendar.getInstance();
		timestamp.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));

		showTaskEditPanel(new Task(timestamp), -1);
	}

	public void saveTask(Task task, int taskPosition) {
		val adapter = (TaskAdapter) binding.tasksContainer.getAdapter();

		if (adapter != null) {
			if (taskPosition >= 0) {
				adapter.notifyItemChanged(taskPosition);
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

				() -> NotificationUtil.updateNotification(this, task)
		);
	}

	private void updateNoTasksText(TaskAdapter adapter) {
		binding.noTasks.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
	}


	// ------------------------------------------ utils -------------------------------------------
	private String formatYearMonth(Calendar yearMonth) {
		int year = yearMonth.get(Calendar.YEAR);
		String month = getResources().getString(monthStringIds[yearMonth.get(Calendar.MONTH)]);
		return year + " " + month;
	}
}