package com.x590.calendar.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.x590.calendar.CalendarApp;
import com.x590.calendar.R;
import com.x590.calendar.database.Task;
import lombok.val;

public class ReminderNotificationReceiver extends BroadcastReceiver {
	private static final String CHANNEL_ID = "calendar_app";
	private static final String CHANNEL_NAME = "Calendar App";

	private static final long[] VIBRATION_PATTERN = {
			0,   500, 250, 500,
			500, 500, 250, 500,
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
			channel.enableVibration(true);
			channel.setVibrationPattern(VIBRATION_PATTERN);

			val notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}

		int id = intent.getIntExtra(MainActivity.TASK_ID_EXTRA, 0);
		if (id == 0) return;

		CalendarApp.databaseRequest(
				database -> database.taskDao().getById(id),
				task -> {
					if (task == null) return;
					sendNotification(context, task);
					NotificationUtil.updateNotification(context, task);
				}
		);
	}

	private void sendNotification(Context context, Task task) {
		val intent = new Intent(context, MainActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		val pendingIntent = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		val notification = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_bell)
				.setContentTitle(context.getString(R.string.reminder))
				.setContentText(task.getDescription())
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setVibrate(VIBRATION_PATTERN)
				.build();

		val notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.notify(task.getId(), notification);
	}
}
