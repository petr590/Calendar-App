package com.x590.calendar;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter @Setter
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String description;

    private Calendar date;

    private boolean reminderEnabled;

    private boolean finished;
}