package com.x590.calendar.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter @Setter
@Entity(tableName = "tasks")
public class Task {
	@PrimaryKey(autoGenerate = true)
	private int id;

	private String description;

	private Calendar timestamp;

	private boolean reminderEnabled;

	private boolean finished;

	public Task() {}

	public Task(Calendar timestamp) {
		this.description = "";
		this.timestamp = timestamp;
	}

	public boolean needNotification() {
		return reminderEnabled && !finished && timestamp.getTimeInMillis() > System.currentTimeMillis();
	}

	/** @return true, если задачу можно сохранить в базе данных, т.е. соблюдаются инварианты */
	public boolean isValid() {
		return description != null && timestamp != null && !description.isEmpty();
	}
}