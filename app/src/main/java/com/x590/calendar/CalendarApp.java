package com.x590.calendar;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.room.Room;
import com.x590.calendar.database.AppDatabase;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class CalendarApp extends Application {
	@Getter
	private static CalendarApp instance;

	@Getter
	private AppDatabase database;

	private ExecutorService executor;

	private Handler mainLoopHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		database = Room.databaseBuilder(this, AppDatabase.class, "database")
				.fallbackToDestructiveMigration()
				.build();

		executor = Executors.newSingleThreadExecutor();
		mainLoopHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Выполняет запрос к базе данных в фоновом потоке и вызывает callback в UI-потоке.
	 * @param request запрос к базе данных, возвращающий какое-то значение.
	 * @param callback функция, которая запускается в потоке UI и принимает результат запроса.
	 */
	public static <T> void databaseRequest(Function<AppDatabase, T> request, Consumer<T> callback) {
		wrapTryCatch(() -> {
			T res = request.apply(instance.database);
			instance.mainLoopHandler.post(() -> callback.accept(res));
		});
	}

	/**
	 * Выполняет запрос к базе данных в фоновом потоке и вызывает callback в UI-потоке.
	 * @param request запрос к базе данных.
	 * @param callback функция, которая запускается в потоке UI.
	 */
	public static void databaseRequest(Consumer<AppDatabase> request, Runnable callback) {
		wrapTryCatch(() -> {
			request.accept(instance.database);
			instance.mainLoopHandler.post(callback);
		});
	}

	/** Выполняет запрос к базе данных в фоновом потоке. */
	public static void databaseRequest(Consumer<AppDatabase> request) {
		wrapTryCatch(() -> request.accept(instance.database));
	}

	private static void wrapTryCatch(Runnable runnable) {
		instance.executor.submit(() -> {
			try {
				runnable.run();
			} catch (Throwable tr) {
				Log.e("DATABASE", "", tr);
				instance.mainLoopHandler.post(CalendarApp::showErrorMessage);
			}
		});
	}

	private static void showErrorMessage() {
		Toast.makeText(instance, R.string.database_error_message, Toast.LENGTH_LONG).show();
	}
}