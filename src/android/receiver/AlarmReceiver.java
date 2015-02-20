package com.google.profile.receiver;

import com.google.profile.FirstPage;
import com.google.profile.R;
import com.google.profile.db.EventModel;
import com.google.profile.db.EventsReminderHandler;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		EventModel eventModel = (EventModel) intent.getExtras().get(
				"eventObject");
		showNotification(context, eventModel);
		scheduleNextEvent(context);
	}

	private void scheduleNextEvent(Context context) {
		EventsReminderHandler erh = new EventsReminderHandler(context);
		EventModel nextEvent = erh.getNextEvent();

		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context.getApplicationContext(), 234324243, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextEvent.getEventStartTime()
				- nextEvent.getReminderTime(), pendingIntent);
	}

	@SuppressWarnings("deprecation")
	private void showNotification(Context context, EventModel eventModel) {
		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		String msg = eventModel.getEventName();
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, msg, when);
		String title = "WotNow Event Reminder";

		Intent notificationIntent = new Intent(context, FirstPage.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(notificationIntent);

		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, msg, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		mNM.notify(0, notification);
	}

}
