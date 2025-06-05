package com.x590.calendar.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import com.x590.calendar.R;
import com.x590.calendar.Util;
import com.x590.calendar.databinding.ItemMonthBinding;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;

public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.MonthViewHolder> {
	/** Количество элементов в адаптере (практически бесконечность) */
	public static final int COUNT = Integer.MAX_VALUE;

	/** Начальный индекс в адаптере */
	public static final int START_INDEX = COUNT / 2;


	/** Начальная дата, соответствующая элементу START_INDEX */
	@Getter(AccessLevel.PACKAGE)
	private final Calendar initialDate = Calendar.getInstance();

	private final Consumer<Calendar> dayChangedListener;
	private @Nullable View currentDayButton;

	public CalendarPagerAdapter(Consumer<Calendar> dayChangedListener) {
		this.dayChangedListener = dayChangedListener;
	}

	public Calendar getCurrentDay() {
		return (Calendar) Objects.requireNonNull(currentDayButton).getTag(R.id.tag_date);
	}

	public Calendar getCurrentMonthByPos(int position) {
		Calendar month = (Calendar) initialDate.clone();
		month.add(Calendar.MONTH, position - START_INDEX);
		return month;
	}

	class MonthViewHolder extends RecyclerView.ViewHolder {
		private static final String TAG_DAY = "day";
		private static final String TAG_WEEK_NUM = "weekNum";

		private final ItemMonthBinding binding;

		public MonthViewHolder(ItemMonthBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(Calendar date) {
			final int month = date.get(Calendar.MONTH);

			date.set(Calendar.DAY_OF_MONTH, 1);
			date.add(Calendar.DAY_OF_WEEK, Util.getDaysOffset(date));
			assert date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;

			Calendar today = Calendar.getInstance();

			TableLayout dayTable = binding.getRoot();

			if (date.get(Calendar.DAY_OF_MONTH) == 1 && date.getActualMaximum(Calendar.DAY_OF_MONTH) == 28) {
				binding.lastCalendarRow.setVisibility(View.GONE);
			}

			for (int i = 0; i < dayTable.getChildCount(); i++) {
				View child1 = dayTable.getChildAt(i);
				if (!(child1 instanceof TableRow) || child1.getVisibility() == View.GONE) continue;
				TableRow row = (TableRow) child1;

				for (int j = 0; j < row.getChildCount(); j++) {
					View child2 = row.getChildAt(j);

					if (TAG_DAY.equals(child2.getTag()) && child2 instanceof Button) {
						Button button = (Button) child2;
						button.setOnClickListener(this::onDayClick);
						button.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
						button.setTag(R.id.tag_date, date.clone());

						if (date.get(Calendar.MONTH) != month) {
							button.setTextAppearance(R.style.Theme_CalendarApp_OtherMonthCell);

						} else if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
									date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
									date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {

							button.setTextAppearance(R.style.Theme_CalendarApp_TodayCell);
							onDayClick(button);
						}

						date.add(Calendar.DAY_OF_MONTH, 1);

					} else if (TAG_WEEK_NUM.equals(child2.getTag()) && child2 instanceof TextView) {
						TextView textView = (TextView) child2;
						textView.setText(String.valueOf(date.get(Calendar.WEEK_OF_YEAR)));
					}
				}
			}
		}

		private void onDayClick(View button) {
			if (currentDayButton != null) {
				currentDayButton.setBackground(null);
			}

			button.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.shape_round_border));
			currentDayButton = button;

			dayChangedListener.accept((Calendar) button.getTag(R.id.tag_date));
		}
	}

	@Override
	public @NotNull MonthViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new MonthViewHolder(ItemMonthBinding.inflate(inflater, parent, false));
	}

	@Override
	public void onBindViewHolder(@NotNull MonthViewHolder holder, int position) {
		holder.bind(getCurrentMonthByPos(position));
	}

	@Override
	public int getItemCount() {
		return COUNT;
	}
}
