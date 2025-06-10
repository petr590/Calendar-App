package com.x590.calendar.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.fragment.app.Fragment;
import com.x590.calendar.database.PeriodUnit;
import com.x590.calendar.database.Task;
import com.x590.calendar.databinding.FragmentTaskEditPanelBinding;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class TaskEditPanelFragment extends Fragment {
	private FragmentTaskEditPanelBinding binding;
	private Task task;
	private int taskPosition;

	@Override
	public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentTaskEditPanelBinding.inflate(inflater, container, false);

		binding.taskTimePicker.setIs24HourView(true);
		binding.saveTaskButton.setOnClickListener(view -> saveTask());
		binding.cancelTaskEditButton.setOnClickListener(view -> hide());

		binding.taskDescription.setOnEditorActionListener((textView, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_NEXT) {
				hideKeyboard();
				return true;
			}

			return false;
		});

		return binding.getRoot();
	}

	private MainActivity requireMainActivity() {
		return (MainActivity) super.requireActivity();
	}

	public void show(Task task, int taskPosition) {
		this.task = task;
		this.taskPosition = taskPosition;

		binding.taskDescription.setText(task.getDescription());
		binding.taskReminderEnabled.setChecked(task.isReminderEnabled());

		binding.taskTimePicker.setHour(task.getTimestamp().get(Calendar.HOUR_OF_DAY));
		binding.taskTimePicker.setMinute(task.getTimestamp().get(Calendar.MINUTE));

		if (task.hasPeriod()) {
			binding.taskPeriod.setText(String.valueOf(task.getPeriod()));
			binding.taskPeriodUnit.setSelection(task.requirePeriodUnit().position());
		} else {
			binding.taskPeriod.setText("");
			binding.taskPeriodUnit.setSelection(PeriodUnit.DAYS.position());
		}

		binding.taskDescription.requestFocus();
		binding.taskDescription.setSelection(binding.taskDescription.getText().length());

		showKeyboard();
	}

	private void hide() {
		this.task = null;
		this.taskPosition = 0;

		hideKeyboard();
		requireMainActivity().hideTaskEditPanel();
	}

	private void showKeyboard() {
		InputMethodManager manager = requireContext().getSystemService(InputMethodManager.class);
		manager.showSoftInput(binding.taskDescription, InputMethodManager.SHOW_IMPLICIT);
	}

	private void hideKeyboard() {
		View view = requireActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager manager = requireContext().getSystemService(InputMethodManager.class);
			manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private int getPeriod() {
		try {
			return Integer.parseInt(binding.taskPeriod.getText().toString());
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private void saveTask() {
		val task = this.task;

		if (task != null) {
			task.setDescription(binding.taskDescription.getText().toString());
			task.setReminderEnabled(binding.taskReminderEnabled.isChecked());
			task.getTimestamp().set(Calendar.HOUR_OF_DAY, binding.taskTimePicker.getHour());
			task.getTimestamp().set(Calendar.MINUTE, binding.taskTimePicker.getMinute());

			int period = getPeriod();
			if (period > 0) {
				task.setPeriod(period);
				task.setPeriodUnit(PeriodUnit.byPosition(binding.taskPeriodUnit.getSelectedItemPosition()));
			} else {
				task.setPeriod(0);
				task.setPeriodUnit(null);
			}

			if (!task.isValid()) return;

			requireMainActivity().saveTask(task, taskPosition);
			hide();
		}
	}
}