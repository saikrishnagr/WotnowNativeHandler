package com.alltivity.wotnow;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.cordova.DroidGap;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.parse.ParseFacebookUtils;

public class FirstPage extends DroidGap {
	static String location, city, state, country;
	static double dlat;
	static double dlon;
	AccountManager mAccountManager;
	String token;
	int serverCode;
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;
	TextView txtLat;
	String lat;
	String provider;
	protected String latitude, longitude;
	protected boolean gps_enabled, network_enabled;
	CClocation objCCl;
	private static String Latitude;
	private static String Longtitude;
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	static final int DATE_DIALOG_ID = 999;
	private int year;
	private int month;
	private int day;

	WotNowApplication aController;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;

	public static String registrationId;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.loadUrl("file:///android_asset/www/index.html");

		objCCl = new CClocation();
		CClocation.setContext(FirstPage.this);
		objCCl.getLocation2();
		Latitude = String.valueOf(objCCl.latitude);
		Longtitude = String.valueOf(objCCl.longitude);

		syncGoogleAccount();

		try {
			getAddress(FirstPage.this, Latitude, Longtitude);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		doGCMController();
		try{
			Log.d("GCMIntentService.Url", GCMIntentService.Url)	;
			super.loadUrl("javascript:pushnotification('" + GCMIntentService.Url.trim() + "')");
		}catch(Exception ex)
		{}

		/*	if (getIntent().getExtras().containsKey("KEY")) {
			try{
				Log.v("Key>>>>>>>", "KEY");
				//String url = GCMIntentService.Url;
				String url = getIntent().getExtras().getString("URL");
				Log.v("URL", url);
				super.loadUrl("javascript:pushnotification('" + url.trim() + "')");
			}catch(Exception ex)
			{}
		}*/
	}

	private void doGCMController() {

		// Get Global Controller Class object (see application tag in
		// AndroidManifest.xml)
		aController = (WotNowApplication) getApplicationContext();

		// Check if Internet present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(FirstPage.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			// stop executing code by return
			return;
		}

		// Getting name, email from intent

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest permissions was properly set
		GCMRegistrar.checkManifest(this);

		// Register custom Broadcast receiver to show messages on activity
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {

			// Register with GCM
			GCMRegistrar.register(this, Config.GOOGLE_SENDER_ID);
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			});

		} else {
			// Skips registration.
			/*
			 * Toast.makeText(getApplicationContext(), regId,
			 * Toast.LENGTH_LONG). show();
			 */
			Log.v("gcm reg id", regId);
			registrationId = regId;

		}

	}

	public BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String newMessage = intent.getExtras().getString(
					Config.EXTRA_MESSAGE);

			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());

			// Display message on the screen

			Toast.makeText(getApplicationContext(),
					"Got Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};

	public static JSONObject convertToJson() throws JSONException {
		JSONObject object = new JSONObject();
		try {
			object.put("Latitude", dlat);
			object.put("Longitude", dlon);
			object.put("Location", location);
			object.put("City", city);
			object.put("State", state);
			object.put("Country", country);
			Log.v("getLocation :", object.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;

	}

	private String[] getAccountNames() {
		mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		String[] names = new String[accounts.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	private AbstractGetNameTask getTask(FirstPage activity, String email,
			String scope) {
		return new GetNameInForeground(activity, email, scope);

	}

	public void syncGoogleAccount() {
		String[] accountarrs = getAccountNames();
		if (accountarrs.length > 0) {
			// you can set here account for login
			getTask(FirstPage.this, accountarrs[0], SCOPE).execute();
		} else {
			// redirect to google browser
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	public String getAddress(Context ctx, String latitude, String longitude)
			throws JSONException {
		StringBuilder result = new StringBuilder();
		try {
			Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

			Double lat = Double.valueOf(latitude);
			Double lon = Double.valueOf(longitude);

			List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				location = address.getAddressLine(0);
				city = address.getLocality();
				country = address.getCountryName();
				dlat = address.getLatitude();
				dlon = address.getLongitude();
				state = address.getAdminArea();
				convertToJson();
				syncGoogleAccount();

			}
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		return result.toString();
	}

	@Override
	public void onDestroy() {
		// Cancel AsyncTask
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			// Unregister Broadcast Receiver
			unregisterReceiver(mHandleMessageReceiver);

			// Clear internal resources.
			GCMRegistrar.onDestroy(this);

		} catch (Exception e) {
			Log.v("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DATE_DIALOG_ID:


			String date = args.getString("Date");
			Log.v("onCreateDialog+date", date);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
			String dateInString = date;



			try {
				Date mdate = formatter.parse(dateInString);
				year = mdate.getYear();
				month = mdate.getMonth();
				day =mdate.getDay();

				Log.v("StrToDate", year + "----" + month + "-----" +day );
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}







			return new DatePickerDialog(FirstPage.this, datePickerListener, year, month,
					day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;
		}
	};
}
