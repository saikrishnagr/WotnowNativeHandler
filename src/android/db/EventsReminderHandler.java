package com.google.profile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EventsReminderHandler {

	private static final String DATABASE_NAME = "wotnow_database";
	private static final String TABLE_NAME = "events_reminder";

	private static final String EVENT_ID = "event_id";
	private static final String EVENT_NAME = "event_name";
	private static final String EVENT_START_TIME = "event_start_time";
	private static final String REMINDER_TIME = "reminder_time";

	private SQLiteDatabase database;
	private DBOpenHelper dbOpenHelper;

	public EventsReminderHandler(Context context) {
		dbOpenHelper = new DBOpenHelper(context, DATABASE_NAME, null, 1);
	}

	public void open() throws SQLException {
		database = dbOpenHelper.getWritableDatabase();
	}

	public void close() {
		if (database != null)
			database.close();
	}

	public void addReminderForEvent(String event_id, String event_name,
			int event_start_time, int reminder_time) {
		open();
		ContentValues field = new ContentValues();
		field.put(EVENT_ID, event_id);
		field.put(EVENT_NAME, event_name);
		field.put(EVENT_START_TIME, event_start_time);
		field.put(REMINDER_TIME, reminder_time);
		database.insert(TABLE_NAME, null, field);
		close();
	}

	public void removeReminderForEvent(String event_id) {
		open();
		database.delete(TABLE_NAME, EVENT_ID + " = " + event_id, null);
		close();
	}

	public void editEventReminder(String event_id, String event_name,
			int event_start_time, int reminder_time) {
		open();
		ContentValues field = new ContentValues();
		field.put(EVENT_NAME, event_name);
		field.put(EVENT_START_TIME, event_start_time);
		field.put(REMINDER_TIME, reminder_time);
		database.update(TABLE_NAME, field, EVENT_ID + " = " + event_id, null);
		close();
	}

	public EventModel getNextEvent() {
		open();
		Cursor cursor = database.query(TABLE_NAME, null, null, null, null,
				null, null);
		int nextNotificationTime = 0;
		EventModel mEventModel = null;
		while (cursor.moveToNext()) {
			int tempNextNotificationTime = cursor.getInt(cursor
					.getColumnIndex(EVENT_START_TIME))
					- cursor.getInt(cursor.getColumnIndex(REMINDER_TIME));
			if (tempNextNotificationTime > nextNotificationTime) {
				mEventModel = new EventModel(cursor.getString(cursor
						.getColumnIndex(EVENT_START_TIME)),
						cursor.getString(cursor
								.getColumnIndex(EVENT_START_TIME)),
						cursor.getInt(cursor.getColumnIndex(EVENT_START_TIME)),
						cursor.getInt(cursor.getColumnIndex(EVENT_START_TIME)));
			}
		}
		cursor.close();
		close();
		return mEventModel;
	}
}