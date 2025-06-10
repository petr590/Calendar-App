package com.x590.calendar.ui;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.x590.calendar.CalendarApp;
import com.x590.calendar.R;
import com.x590.calendar.database.PeriodUnit;
import com.x590.calendar.database.Task;
import com.x590.calendar.databinding.ItemTaskBinding;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
	private final List<Task> tasks;

	public TaskAdapter(List<Task> tasks) {
		this.tasks = new ArrayList<>(tasks);
	}

	
	static class TaskViewHolder extends RecyclerView.ViewHolder {
		private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
		private final TaskAdapter adapter;
		private final ItemTaskBinding binding;
		private Task task;

		public TaskViewHolder(TaskAdapter adapter, ItemTaskBinding binding) {
			super(binding.getRoot());
			this.adapter = adapter;
			this.binding = binding;
		}

		private MainActivity getMainActivity() {
			return (MainActivity) itemView.getContext();
		}

		public void bind(Task task) {
			this.task = task;

			binding.taskDescription.setText(task.getDescription());
			binding.taskTime.setText(TIME_FORMAT.format(task.getTimestamp().getTime()));
			binding.isTaskFinished.setChecked(task.isFinished());
			binding.taskReminderIcon.setVisibility(task.isReminderEnabled() ? View.VISIBLE : View.GONE);

			if (task.hasPeriod()) {
				binding.taskPeriod.setText(getPeriodString(task.getPeriod(), task.requirePeriodUnit()));
				binding.taskPeriod.setVisibility(View.VISIBLE);
			} else {
				binding.taskPeriod.setText("");
				binding.taskPeriod.setVisibility(View.GONE);
			}

			itemView.setOnClickListener(itemView -> getMainActivity().showTaskEditPanel(task, getAdapterPosition()));

			binding.isTaskFinished.setOnCheckedChangeListener((checkBox, checked) -> {
				task.setFinished(checked);
				NotificationUtil.updateNotification(itemView.getContext(), task);
				CalendarApp.databaseRequest(database -> database.taskDao().update(task));
			});

			binding.deleteIcon.setOnClickListener(button -> confirmDelete());
		}

		private void confirmDelete() {
			val message = new TextView(itemView.getContext());
			message.setText(R.string.confirm_delete);
			message.setGravity(Gravity.CENTER);
			message.setPadding(50, 50, 50, 50);

			val dialog = new AlertDialog.Builder(itemView.getContext())
					.setView(message)
					.setPositiveButton(R.string.delete, (dialogInterface, id) -> deleteTask())
					.setNegativeButton(R.string.cancel, (dialogInterface, id) -> {})
					.show();

			Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

			val layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
			layoutParams.weight = 10;

			btnPositive.setLayoutParams(layoutParams);
			btnNegative.setLayoutParams(layoutParams);
		}

		private void deleteTask() {
			adapter.deleteTask(getAdapterPosition());
			NotificationUtil.removeNotification(itemView.getContext(), task);
			CalendarApp.databaseRequest(database -> database.taskDao().delete(task));
		}

		private String getPeriodString(int period, PeriodUnit unit) {
			if (period == 1) {
				String[] array = getMainActivity().getResources().getStringArray(R.array.every_period_unit);
				return array[unit.position()];
			}

			return getMainActivity().getResources().getQuantityString(
					task.requirePeriodUnit().getPluralId(), period, period
			);
		}
    }

	@Override
	public @NotNull TaskViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new TaskViewHolder(this, ItemTaskBinding.inflate(inflater, parent, false));
	}

	@Override
	public void onBindViewHolder(@NotNull TaskViewHolder holder, int position) {
		holder.bind(tasks.get(position));
	}

	@Override
	public int getItemCount() {
		return tasks.size();
	}

	void addTask(Task task) {
		tasks.add(task);
		notifyItemInserted(tasks.size() - 1);
	}

	private void deleteTask(int position) {
		tasks.remove(position);
		notifyItemRemoved(position);
	}
}