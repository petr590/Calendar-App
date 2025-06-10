package com.x590.calendar.database;

import androidx.room.*;
import com.x590.calendar.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;

@Dao
public interface TaskDao {
	@Query("SELECT * FROM tasks WHERE id = :id")
	@Nullable Task getById(int id);

	@Query("SELECT * FROM tasks " +
			"WHERE timestamp BETWEEN :from AND :to " +
			"ORDER BY timestamp")
	List<Task> getByTimestampBetween(Calendar from, Calendar to);

	default List<Task> getByDay(Calendar date) {
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH);
		int day = date.get(Calendar.DAY_OF_MONTH);

		return getByTimestampBetween(
				Util.getDay(year, month, day),
				Util.getDay(year, month, day + 1)
		);
	}

	default int insert(Task task) {
		return (int) doInsert(task);
	}

	@Insert
	long doInsert(Task task);

	@Update
	void update(Task task);

	@Delete
	void delete(Task task);

	/** @deprecated Debug only! */
	@Deprecated
	@Query("DELETE FROM tasks")
	void clear();
}