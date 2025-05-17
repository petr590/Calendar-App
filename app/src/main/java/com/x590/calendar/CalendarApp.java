package com.x590.calendar;

import android.app.Activity;
import android.app.Application;
import androidx.room.Room;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class CalendarApp extends Application {
	@Getter
	private static CalendarApp instance;

	private AppDatabase database;

	private ExecutorService executor;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		database = Room.databaseBuilder(this, AppDatabase.class, "database").build();
		executor = Executors.newSingleThreadExecutor();
	}
	
	public static <T> void databaseRequest(Activity activity, Function<AppDatabase, T> request, Consumer<T> callback) {
		instance.executor.submit(() -> {
			T res = request.apply(instance.database);
			activity.runOnUiThread(() -> callback.accept(res));
		});
	}

	public static void databaseRequest(Consumer<AppDatabase> request) {
		instance.executor.submit(() -> request.accept(instance.database));
	}
}
