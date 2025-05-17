package com.x590.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.x590.calendar.databinding.ItemTaskBinding;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
	private final List<Task> tasks;

	public TaskAdapter(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	static class TaskViewHolder extends RecyclerView.ViewHolder {
		private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
		private final ItemTaskBinding binding;

		public TaskViewHolder(ItemTaskBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(Task task) {
			binding.tvTaskDescription.setText(task.getDescription());
			binding.tvTaskTime.setText(TIME_FORMAT.format(task.getDate().getTime()));
			binding.cbTaskFinished.setChecked(task.isFinished());
			binding.ivTaskReminder.setVisibility(task.isReminderEnabled() ? View.VISIBLE : View.GONE);

			binding.cbTaskFinished.setOnCheckedChangeListener((checkBox, checked) -> {
				task.setFinished(checked);
				CalendarApp.databaseRequest(database -> database.taskDao().update(task));
			});
		}
    }

	@Override
	public @NotNull TaskViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new TaskViewHolder(ItemTaskBinding.inflate(inflater, parent, false));
	}

	@Override
	public void onBindViewHolder(@NotNull TaskViewHolder holder, int position) {
		holder.bind(tasks.get(position));
	}

	@Override
	public int getItemCount() {
		return tasks.size();
	}
}
