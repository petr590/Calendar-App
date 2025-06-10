package com.x590.calendar.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Objects;

@Getter @Setter
@Entity(tableName = "tasks")
public class Task {
	@PrimaryKey(autoGenerate = true)
	private int id;

	private String description;

	private Calendar timestamp;

	private int period;
	private @Nullable PeriodUnit periodUnit;

	private boolean reminderEnabled;
	private boolean finished;

	public Task() {}

	public Task(Calendar timestamp) {
		this.description = "";
		this.timestamp = timestamp;
	}

	/** @return true, если задачу можно сохранить в базе данных, т.е. соблюдаются инварианты */
	public boolean isValid() {
		return description != null && timestamp != null && !description.isEmpty();
	}

	public boolean hasPeriod() {
		return periodUnit != null && period > 0;
	}

	public @NotNull PeriodUnit requirePeriodUnit() {
		return Objects.requireNonNull(periodUnit);
	}


	/** @return Следующий момент времени, когда надо показать уведомление.
	 * Если есть период, то рассчитывает с учётом периода. */
	public @Nullable Calendar getNotificationTimestamp() {
		if (!reminderEnabled || finished) {
			return null;
		}

		if (timestamp.getTimeInMillis() > System.currentTimeMillis()) {
			return timestamp;
		}

		if (hasPeriod()) {
			val result = (Calendar) timestamp.clone();
			val unit = requirePeriodUnit();

			while (result.getTimeInMillis() < System.currentTimeMillis()) {
				result.add(unit.getType(), period);
			}

			return result;
		}

		return null;
	}
}