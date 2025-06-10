package com.x590.calendar.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.x590.calendar.R;
import com.x590.calendar.Util;
import com.x590.calendar.database.Task;
import lombok.val;

import java.util.Calendar;

public class NotificationUtil {
	public static final String TASK_ID_EXTRA = "taskId";

	public static void updateNotification(Context context, Task task) {
		Calendar timestamp = task.getNotificationTimestamp();

		if (timestamp != null) {
			addNotification(context, task, timestamp);
		} else {
			removeNotification(context, task);
		}
	}

	public static void addNotification(Context context, Task task, Calendar timestamp) {
		val alarmManager = context.getSystemService(AlarmManager.class);

		alarmManager.setExactAndAllowWhileIdle(
				AlarmManager.RTC_WAKEUP,
				timestamp.getTimeInMillis(),
				getBroadcastPendingIntent(context, task)
		);

		val dateTime = Util.getDateTimeFormat(context).format(timestamp.getTime());
		Toast.makeText(context, context.getString(R.string.reminder_set_for, dateTime), Toast.LENGTH_LONG).show();
	}


	public static void removeNotification(Context context, Task task) {
		val alarmManager = context.getSystemService(AlarmManager.class);
		alarmManager.cancel(getBroadcastPendingIntent(context, task));

//		String dateTime = Util.getDateTimeFormat(context).format(task.getTimestamp().getTime());
//		Toast.makeText(context, String.format("Removed: %s", dateTime), Toast.LENGTH_SHORT).show();
	}


	private static PendingIntent getBroadcastPendingIntent(Context context, Task task) {
		assert task.getId() != 0;

		val intent = new Intent(context, ReminderNotificationReceiver.class)
				.setData(Uri.parse("calendar-app://task/" + task.getId()))
				.putExtra(TASK_ID_EXTRA, task.getId());

		return PendingIntent.getBroadcast(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
	}
}
