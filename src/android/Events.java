package com.google.profile;

import java.io.Serializable;

public class Events implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*url":"",
    "id":"F6B52A4D-9F9F-44BF-ACE9-44C08A8C2546:3B83CF58-EB8C-4BDD-AED7-42B2D3446E75",
    "endDate":"2014-12-08T21:00:00+0530",
    "timezone":"GMT+5:30",
    "note":"",
    "location":"chennai",
    "startDate":"2014-12-08T20:00:00+0530",
    "title":"testing event"*/
	
	public String url;
	public int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String endDate;
	public String timezone;
	public String note;
	public String location;
	public String startDate;
	public String title;
	public String desc;
	
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	

}
