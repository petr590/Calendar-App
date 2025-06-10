package com.x590.calendar.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(version = 2, entities = Task.class)
@TypeConverters({ CalendarConverter.class, PeriodUnit.Converter.class })
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}