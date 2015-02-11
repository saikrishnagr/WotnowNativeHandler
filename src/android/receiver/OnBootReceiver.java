package com.google.profile.receiver;

import com.google.profile.db.EventModel;
import com.google.profile.db.EventsReminderHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		EventsReminderHandler erh = new EventsReminderHandler(context);
		EventModel nextEvent = erh.getNextEvent();

		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context.getApplicationContext(), 234324243, alarmIntent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextEvent.getEventStartTime()
				- nextEvent.getReminderTime(), pendingIntent);
	}
}