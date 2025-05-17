package com.x590.calendar;

import androidx.room.*;

import java.util.Calendar;
import java.util.List;

@Dao
public interface TaskDao {
	@Query("SELECT * FROM tasks")
	List<Task> getAll();

	@Query("SELECT * FROM tasks " +
			"WHERE date BETWEEN :from AND :to " +
			"ORDER BY date")
	List<Task> getByDateBetween(Calendar from, Calendar to);

	default List<Task> getByDay(Calendar date) {
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH);
		int day = date.get(Calendar.DAY_OF_MONTH);

		return getByDateBetween(
				getDay(year, month, day),
				getDay(year, month, day + 1)
		);
	}

	static Calendar getDay(int year, int month, int day) {
		Calendar date = Calendar.getInstance();
		date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}

	@Query("SELECT * FROM tasks WHERE id = :id")
	Task getById(long id);

	@Insert
	void insert(Task task);

	@Update
	void update(Task task);

	@Delete
	void delete(Task task);

	/** @deprecated Debug only! */
	@Deprecated
	@Query("DELETE FROM tasks")
	void clear();
}
