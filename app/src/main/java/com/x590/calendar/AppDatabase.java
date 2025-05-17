package com.x590.calendar;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(version = 1, entities = Task.class)
@TypeConverters(CalendarConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
