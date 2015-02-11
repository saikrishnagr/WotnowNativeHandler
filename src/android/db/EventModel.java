package com.google.profile.db;

public class EventModel {
	private String eventId;
	private String eventName;
	private int eventStartTime;
	private int reminderTime;

	public EventModel(String eventId, String eventName, int eventStartTime,
			int reminderTime) {
		super();
		this.eventId = eventId;
		this.eventName = eventName;
		this.eventStartTime = eventStartTime;
		this.reminderTime = reminderTime;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getEventStartTime() {
		return eventStartTime;
	}

	public void setEventStartTime(int eventStartTime) {
		this.eventStartTime = eventStartTime;
	}

	public int getReminderTime() {
		return reminderTime;
	}

	public void setReminderTime(int reminderTime) {
		this.reminderTime = reminderTime;
	}
}
