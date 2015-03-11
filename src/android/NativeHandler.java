package com.alltivity.wotnow;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class NativeHandler extends CordovaPlugin implements
		ConnectionCallbacks, OnConnectionFailedListener {
	private static final String Google_Sign_In = "googlePlusSignInAction";
	private static final String Facebook = "facebookSignInAction";
	private static final String Yahoo_Sign_In_Button_Click = "yahooSignInAction";
	private static final String GetLocalContacts = "fetchTheDeviceContacts";
	private static final String GetPhoneEvents = "AndroidEventSync";
	private static final String DeviceId = "addApnsdevice";
	private static final String sharemail = "shareemail";
	private static final String sendSMS = "smsPostAction";
	private static final String androiddatepicker = "androidDatePicker";
	private static final String androidtimepicker = "androidTimePicker";
	private static final String androidAddReminder = "androidAddReminder";
	private static final String twitterShare = "twitterPostAction";
	private static final String openBrowser = "openTheLinkInSafari";
	private static final String openGoogleMaps = "accessTheMap";
	private static final String fetchDeviceLocation = "fetchTheDeviceLocation";
	private String date;
	private JSONObject FBDetails = new JSONObject();
	private String userId;
	private String eventid;
	private ArrayList<FbEvents> eventsObj = null;
	//	private String fbEvents;
//	private static final int DATE_DIALOG_ID = 999;
	
	private CClocation objCCl;
	private static String Latitude;
	private static String Longtitude;

//	private UiLifecycleHelper uiHelper;
	private String RegId =  FirstPage.registrationId;
	
	private final String TAG = "NativeHandler";

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		mContext = this.cordova.getActivity().getApplicationContext();
		mCordovaActivity = this.cordova.getActivity();
		savedCallbackContext = callbackContext;
		String ContactsJson;

		JSONObject profileData = null;
		String eventsJson;
		Log.d(TAG, "Action Called : " + action);
		if (Google_Sign_In.equals(action)) {
			GOOGLE_LOGIN = false;
			if (args.getInt(0) == 1) {
				Intent shareIntent = new PlusShare.Builder(
						mCordovaActivity).setType("text/plain")
						.setText(args.getString(1))
						.setContentUrl(Uri.parse("https://wotnow.me/"))
						.getIntent();

				mCordovaActivity.startActivityForResult(shareIntent, 0);
			} else if (args.get(0).equals("2")) {
				accessGoogleCalendar();
//				syncGoogleCalendar(savedCallbackContext);
			} else {
				GOOGLE_LOGIN = true;
				mGoogleApiClient.connect();
			}
		} else if (Facebook.equals(action)) {
			Log.d(Facebook, "Facebook Sign In Button Clicked: " + action);
			if (args.getInt(0) == 1) {
//				uiHelper = new UiLifecycleHelper(mCordovaActivity,
//						null);
				/*FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
						mCordovaActivity)
				.setLink("http://wotnow.me")
				.setDescription(args.getString(1)).build();
				uiHelper.trackPendingDialogCall(shareDialog.present());*/

//				String urlToShare = args.getString(1);
				String urlToShare = "http://wotnow.me";
				Log.d("TEST", urlToShare);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain"); //
//				intent.putExtra(Intent.EXTRA_SUBJECT, ""); // NB: has
				intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

				// See if official Facebook app is found boolean
				boolean facebookAppFound = false;
				List<ResolveInfo> matches = mCordovaActivity
						.getPackageManager().queryIntentActivities(intent, 0);
				for (ResolveInfo info : matches) {
					if (info.activityInfo.packageName.toLowerCase().startsWith(
							"com.facebook.katana")) {
						intent.setPackage(info.activityInfo.packageName);
						facebookAppFound = true;
						break;
					}
				}

				// As fallback, launch sharer.php in a browser
				if (!facebookAppFound) {
					String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u="
							+ urlToShare;
					intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(sharerUrl));
				}
				mCordovaActivity.startActivity(intent);

			} else
				getFbDetails(callbackContext, args);
		} else if (twitterShare.equals(action)) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, args.getString(0));
			boolean facebookAppFound = false;
			List<ResolveInfo> matches = mCordovaActivity
					.getPackageManager().queryIntentActivities(intent, 0);
			for (ResolveInfo info : matches) {
				if (info.activityInfo.packageName.toLowerCase().startsWith(
						"com.twitter.android")) {
					intent.setPackage(info.activityInfo.packageName);
					facebookAppFound = true;
					break;
				}
			}
			Log.d("TEST", "" + facebookAppFound);
			if(facebookAppFound)
				mCordovaActivity.startActivity(intent);
			else {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/share?text=" + args.getString(0)));
				mCordovaActivity.startActivity(browserIntent);
			}	
		} else if (Yahoo_Sign_In_Button_Click.equals(action)) {
			new getAuthorizationTask().execute();
		} else if (GetLocalContacts.equals(action)) {
			ContactsJson = getContactDetails();
			callbackContext.success(ContactsJson);
		} else if (GetPhoneEvents.equals(action)) {
			Log.d(GetPhoneEvents, "Phone Events: " + action);
			eventsJson = getPhoneEvents();
			Log.d("Local Events", eventsJson);
			callbackContext.success(eventsJson);
		} else if (DeviceId.equals(action)) {
			Log.d(DeviceId, "Phone Events: " + action);
			String regId =RegId;
			callbackContext.success(regId);
		} else if (sendSMS.equals(action)) {
			sendSMS(args.get(0).toString());
		} else if (sharemail.equals(action)) {
			sendEmail(args.get(0).toString());
		} else if (fetchDeviceLocation.equals(action)) {
			objCCl = new CClocation();
			CClocation.setContext(mContext);
			objCCl.getLocation2();
			Latitude = String.valueOf(objCCl.latitude);
			Longtitude = String.valueOf(objCCl.longitude);
			JSONObject locationObject = null;
			try {
				locationObject = getAddress(mContext, Latitude, Longtitude);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(!locationObject.has("City")) {
				locationObject.put("City", "Chicago");
				locationObject.put("State", "IL");
			}
			Log.d(TAG, locationObject.toString());
			callbackContext.success(locationObject);
		} else if (androidAddReminder.equals(action)) {
//			TODO
		} else if (androidtimepicker.equals(action)) {
			final Calendar c = Calendar.getInstance();
			int mHour = c.get(Calendar.HOUR_OF_DAY);
			int mMinute = c.get(Calendar.MINUTE);
			TimePickerDialog tpd = new TimePickerDialog(mContext,
					new TimePickerDialog.OnTimeSetListener() {

				@Override
				public void onTimeSet(TimePicker view, int hourOfDay,
						int minute) {
					String time = hourOfDay + ":" + minute;
					callbackContext.success(time);
				}
			}, mHour, mMinute, false);
			tpd.show();
		} else if (androiddatepicker.equals(action)) {
			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog dpd = new DatePickerDialog(mContext,
					new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					view.updateDate(year, monthOfYear, dayOfMonth);
					date = dayOfMonth + "-" + monthOfYear + "-" + year;
					callbackContext.success(date);
				}
			}, mYear, mMonth, mDay);
			dpd.show();
		} else if (openBrowser.equals(action)) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(args.get(0).toString()));
			mCordovaActivity.startActivity(browserIntent);
		} else if (openGoogleMaps.equals(action)) {
			String uri = String.format(Locale.ENGLISH, "geo:0,0?q=" + args.getString(0));
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			mCordovaActivity.startActivity(intent);
		} else {
			try {
				profileData = FirstPage.convertToJson();
				//Log.v("Location", profileData + "");
				// String regId = FirstPage.registrationId;
				// //Log.v("NativeHandler--Device Id" , regId);
				// This will return the device location details in json format.
				callbackContext.success(profileData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// result = new PluginResult(Status.OK, profileData);
		// return true;
		PluginResult pluginResult = new PluginResult(
				PluginResult.Status.NO_RESULT);
		pluginResult.setKeepCallback(true);
		return true;
	}

	private String getContactDetails() {
		ArrayList<String> emlRecs = new ArrayList<String>();
		HashSet<String> emlRecsHS = new HashSet<String>();
		ArrayList<Contacts> contactsObj = new ArrayList<Contacts>();
		ContentResolver cr = mContext.getContentResolver();
		String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID, 
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.PHOTO_ID,
				ContactsContract.CommonDataKinds.Email.DATA, 
				ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
		String order = "CASE WHEN " 
				+ ContactsContract.Contacts.DISPLAY_NAME 
				+ " NOT LIKE '%@%' THEN 1 ELSE 2 END, " 
				+ ContactsContract.Contacts.DISPLAY_NAME 
				+ ", " 
				+ ContactsContract.CommonDataKinds.Email.DATA
				+ " COLLATE NOCASE";
		String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
		Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
		if (cur.moveToFirst()) {
			do {
				String name = cur.getString(1);
				String emlAddr = cur.getString(3);

	            if (emlRecsHS.add(emlAddr.toLowerCase())) {
	            	Contacts contact = new Contacts();
	            	contact.setNames(name);
	            	contact.setEmail(emlAddr);
	            	contactsObj.add(contact);
	                emlRecs.add(emlAddr);
	            }
	        } while (cur.moveToNext());
	    }

	    cur.close();
	    Gson gson = new Gson();
	    Log.d("TEST", gson.toJson(contactsObj));
		return gson.toJson(contactsObj);
	}
	
	private JSONObject getAddress(Context ctx, String latitude, String longitude)
			throws JSONException {
		JSONObject object = new JSONObject();
		try {
			Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

			Double lat = Double.valueOf(latitude);
			Double lon = Double.valueOf(longitude);

			List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				try {
					object.put("Latitude", address.getLatitude());
					object.put("Longitude", address.getLongitude());
					object.put("Location", address.getAddressLine(0));
					object.put("City", address.getLocality());
					object.put("State", address.getAdminArea());
					object.put("Country", address.getCountryName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return object;
	}

	private void getFbDetails(final CallbackContext callbackContext,
			final JSONArray args) {
		Log.d(Facebook, "Inside the getFbDetails." + Facebook);
		try {
			PackageInfo info = cordova
					.getActivity()
					.getPackageManager()
					.getPackageInfo("com.alltivity.wotnow",
							PackageManager.GET_SIGNATURES);
			for (android.content.pm.Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d(Facebook,
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
				//Log.v("KeyHash:",
//						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}

		Parse.initialize(cordova.getActivity(), "YOUR_PARSE_APPLICATION_ID",
				"YOUR_PARSE_CLIENT_ID");

		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(cordova.getActivity().getString(
				R.string.app_id));

		List<String> permissions = Arrays.asList("public_profile", "email");
		// NOTE: for extended permissions, like "user_about_me", your app
		// must be reviewed by the Facebook team
		// (https://developers.facebook.com/docs/facebook-login/permissions/)

		ParseFacebookUtils.logIn(permissions, cordova.getActivity(),
				new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException err) {
				if (user == null) {
					Log.v(IntegratingFacebookApplication.TAG,
							"Uh oh. The user cancelled the Facebook login.");
				} else if (user.isNew()) {
					Log.v(IntegratingFacebookApplication.TAG,
							"User signed up and logged in through Facebook!");
					showUserDetailsActivity(callbackContext, args);
				} else {
					Log.v(IntegratingFacebookApplication.TAG,
							"User logged in through Facebook!");
					showUserDetailsActivity(callbackContext, args);
				}
			}
		});

	}

	private void showUserDetailsActivity(CallbackContext callbackContext,
			JSONArray args) {

		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest(callbackContext, args);
		}
	}

	private void makeMeRequest(final CallbackContext callbackContext,
			final JSONArray args) {
		final JSONObject userProfile = new JSONObject();
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					// Create a JSON object to hold the profile info

					//Log.v("FBuserProfileresponse", response + "");

					try {
						// Populate the JSON object
						userId = user.getId();
						userProfile.put("id", user.getId());
						userProfile.put("name", user.getName());
						if (user.getProperty("gender") != null) {
							userProfile.put("gender",
									(String) user.getProperty("gender"));
						}
						if (user.getProperty("email") != null) {
							userProfile.put("email",
									(String) user.getProperty("email"));
						}

						if (user.getProperty("first_name") != null) {
							userProfile.put("first_name", (String) user
									.getProperty("first_name"));
						}

						if (user.getProperty("last_name") != null) {
							userProfile.put("last_name", (String) user
									.getProperty("last_name"));
						}
						String isFBLogin = "YES";
						userProfile.put("isFirstTime", isFBLogin);

						// Save the user profile info in a user property
						ParseUser currentUser = ParseUser
								.getCurrentUser();
						currentUser.put("profile", userProfile);
						currentUser.saveInBackground();

						//Log.v("FBuserProfile", userProfile + "");
						//Log.v(Facebook, "Facebook Data: " + userProfile);

						// This will return the FB User details in json
						// format.

						if (args.get(0).equals("1")) {
							//Log.v("FacebookArgs", args.get(0) + "");
							getFBEvents(callbackContext);
						} else if (args.get(0).equals("3")) {
							//Log.v("FacebookArgs", args.get(0) + "");
							// String fdf = (String) args.get(1);
							callFBEvents(args.get(1).toString(),
									callbackContext);
						} else {
							//Log.v("FacebookArgs", args.get(0) + "");
							callbackContext.success(userProfile);
						}
						// callbackContext.success(userProfile);
					} catch (JSONException e) {
						Log.d(IntegratingFacebookApplication.TAG,
								"Error parsing returned user data. "
										+ e);
					}

				} else if (response.getError() != null) {
					if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
							|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
						Log.d(IntegratingFacebookApplication.TAG,
								"The facebook session was invalidated."
										+ response.getError());

					} else {
						Log.d(IntegratingFacebookApplication.TAG,
								"Some other error: "
										+ response.getError());
					}
				}
			}
		});
		request.executeAsync();
	}

	private String getPhoneEvents() throws JSONException {
		ArrayList<Events> eventsObj;
//		JSONObject contactObj;
		Events events;

		Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
		Cursor cursors = cordova.getActivity().getContentResolver()
				.query(CALENDAR_URI, null, null, null, null);

		eventsObj = new ArrayList<Events>();
//		HashMap<String, Events> map = new HashMap<String, Events>();
		Boolean hasPosition = false;
		if (cursors.getCount() > 0) {
			if (cursors.getPosition() < 0)
				hasPosition = cursors.moveToFirst();
			else
				hasPosition = cursors.moveToNext();
			while (hasPosition)

			{

//				map = new HashMap<String, Events>();
				events = new Events();
				String desc = cursors.getString(cursors
						.getColumnIndex("description"));
				events.setDesc(desc);
				String location = cursors.getString(cursors
						.getColumnIndex("eventLocation"));
				events.setLocation(location);
				String id = cursors.getString(cursors.getColumnIndex("_id"));
				events.setId(Integer.parseInt(id));
				String title = cursors.getString(cursors
						.getColumnIndex("title"));
				events.setTitle(title);
				String startDate = cursors.getString(cursors
						.getColumnIndex("dtstart"));

				long number = Long.valueOf(startDate);
//				Log.d("WotNowApp", "Long value : " + number);
				Timestamp stamp = new Timestamp(number);
				Date date = new Date(stamp.getTime());
//				Log.d("WotNowApp", "Date value : " + date.toString());
				// ////Log.v("StartDate", date.toString() + "");

				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss");

				String DateToStr = format.format(date);
				// Log.d("WotNowApp", "DateToStr value : " + DateToStr);

//				SimpleDateFormat format1 = new SimpleDateFormat("z");

//				String DateToStr1 = format1.format(date);
				// Log.d("WotNowApp", "DateToStr1 value : " + DateToStr1);

//				String removeGmt = DateToStr1.replace("GMT", "");
				// Log.d("WotNowApp", "removeGmt value : " + removeGmt);

				String strStartDate = DateToStr;// + removeCharAt(removeGmt, 3);
				// Log.d("WotNowApp", "strStartDate value : " + strStartDate);

				events.setStartDate(strStartDate);
				String endDate = cursors.getString(cursors
						.getColumnIndex("dtend"));

				if (endDate != null) {

					long datetol = Long.valueOf(endDate);
					Timestamp tstamp = new Timestamp(datetol);
					Date date1 = new Date(tstamp.getTime());
					// ////Log.v("EndDate", date1.toString() + "");

					SimpleDateFormat endformat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss");

					String DateToStr3 = endformat.format(date1);

//					SimpleDateFormat formatz = new SimpleDateFormat("z");

//					String DateToStr4 = formatz.format(date1);

//					String removeGmt1 = DateToStr4.replace("GMT", "");

					String strEndDate = DateToStr3;// + removeCharAt(removeGmt1,
					// 3);
					events.setEndDate(strEndDate);
				}

				eventsObj.add(events);
				hasPosition = cursors.moveToNext();
			}
		}
		Gson gson = new Gson();
//		String jsonContacts = gson.toJson(eventsObj);

		return gson.toJson(eventsObj);
	}

	public static String removeCharAt(String s, int pos) {
		return s.substring(0, pos) + s.substring(pos + 1);
	}

	public void FBDetailsSetter(JSONObject userProfile) {
		FBDetails = userProfile;
	}

	public JSONObject FBDetailsgetter() {
		return FBDetails;
	}

	int arrylength;

	private void getFBEvents(final CallbackContext callbackContext) {
		Calendar calendar = Calendar.getInstance();
		//Log.v("CurrentDate", String.valueOf(calendar.getTimeInMillis() / 1000L));

		calendar.add(Calendar.DAY_OF_MONTH, +30);
		//Log.v("FutureDate", String.valueOf(calendar.getTimeInMillis() / 1000L));
		String strFuture = String.valueOf(calendar.getTimeInMillis() / 1000L);

		calendar.add(Calendar.DAY_OF_MONTH, -60);
		//Log.v("PastDate", String.valueOf(calendar.getTimeInMillis() / 1000L));
		String strPast = String.valueOf(calendar.getTimeInMillis() / 1000L);
		String accesstoken = "CAAIxoyLlnhABAHqgBC0ATS528AHuADuTpkYITjhF1SJeZCHZAxp8h6NRthqovBDZAVToX7rf4atDGcTZB3ALS6JIuqz1Dil1ZCQF8H2Ym80dQf3tjDq5CYdFsa3kfrkZBj0hCeN7Re2Eu7HUDSMt062bT0wtr1aHwrRB9puvLS7FWoYIShxWFtvR95zY68QqaEuZBcsGZAxqwZBcFzsHWHAR0";
		String token = "access_token=" + accesstoken + "";
		String future = "since=" + strPast + "";
		String past = "until=" + strFuture + "";
		String fields = "fields=location";
		String limit = "limit=3";
		final Bundle params = new Bundle();
		// params.putString("fields", token + future + past + fields);
		params.putString("access_token", accesstoken);
		params.putString("since", strPast);
		params.putString("until", strFuture);
		// params.putString("fields", "location,name");

		new Request(ParseFacebookUtils.getSession(), "/" + userId + "/events",
				params, HttpMethod.GET, new Request.Callback() {
			public void onCompleted(Response response) {
				//Log.v("Params", params + "");
				//Log.v("userId", userId);
				//Log.v("Response", response.getRawResponse() + "");
				String json = response.getRawResponse();
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(json);
					JSONArray jarry = jsonObj.getJSONArray("data");
					callbackContext.success(jarry);
					arrylength = jarry.length();
					//Log.v("arrylength", arrylength + "");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// callEventId(response, callbackContext);
			}

		}).executeAsync();

		// ParseUser.logOut();
		// startLoginActivity();
	}

	private void callFBEvents(final String streventId,
			final CallbackContext callbackContext) {
		new Request(ParseFacebookUtils.getSession(), "/" + streventId + "",
				null, HttpMethod.GET, new Request.Callback() {

			public void onCompleted(Response response) {
				//Log.v("EventId", streventId);
				//Log.v("Response", response.getRawResponse() + "");

				callbackContext.success(response.getRawResponse());
				/*
				 * FbEvents fbeventsobj = new FbEvents(); String json =
				 * response.getRawResponse(); String
				 * id,timezone,location,desc,name,privacy,startTime;
				 * 
				 * try { JSONObject mjsonObj = new JSONObject(json); id
				 * = mjsonObj.getString("id"); timezone =
				 * mjsonObj.getString("timezone"); location =
				 * mjsonObj.getString("location"); desc =
				 * mjsonObj.getString("description"); name =
				 * mjsonObj.getString("name"); privacy =
				 * mjsonObj.getString("privacy"); startTime =
				 * mjsonObj.getString("start_time");
				 * 
				 * fbeventsobj.setId(id);
				 * fbeventsobj.setTimezone(timezone);
				 * fbeventsobj.setLocation(location);
				 * fbeventsobj.setDesc(desc); fbeventsobj.setName(name);
				 * fbeventsobj.setPrivacy(privacy);
				 * fbeventsobj.setStartTime(startTime);
				 * 
				 * } catch (JSONException e) { 
				 * catch block e.printStackTrace(); }
				 * 
				 * eventsObj.add(fbeventsobj); //Log.v("EventsObj",
				 * eventsObj.size() + ""); Gson gson = new Gson();
				 * fbEvents = gson.toJson(eventsObj);
				 * //Log.v("FBEvents-------->", gson.toJson(eventsObj));
				 * callbackContext.success(gson.toJson(eventsObj));
				 * 
				 * }
				 */
			}
		}).executeAsync();

	}

	private void callEventId(Response response,
			final CallbackContext callbackContext) {
		String accesstoken = "CAAIxoyLlnhABAHqgBC0ATS528AHuADuTpkYITjhF1SJeZCHZAxp8h6NRthqovBDZAVToX7rf4atDGcTZB3ALS6JIuqz1Dil1ZCQF8H2Ym80dQf3tjDq5CYdFsa3kfrkZBj0hCeN7Re2Eu7HUDSMt062bT0wtr1aHwrRB9puvLS7FWoYIShxWFtvR95zY68QqaEuZBcsGZAxqwZBcFzsHWHAR0";
		String token = "access_token=" + accesstoken + "";
		eventsObj = new ArrayList<FbEvents>();

		String json = response.getRawResponse();
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(json);

			final JSONArray jarry = jsonObj.getJSONArray("data");

			for (arrylength = 0; arrylength < jarry.length(); arrylength++) {
				JSONObject c = jarry.getJSONObject(arrylength);
				eventid = c.getString("id");

				new Request(ParseFacebookUtils.getSession(),
						"/" + eventid + "", null, HttpMethod.GET,
						new Request.Callback() {

					public void onCompleted(Response response) {
						//Log.v("EventId", eventid);
						//Log.v("Response", response.getRawResponse()+ "");
						arrylength--;
						//Log.v("arrylength", arrylength + "");
						FbEvents fbeventsobj = new FbEvents();
						String json = response.getRawResponse();
						String id;
						String timezone;
						String location;
						String desc;
						String name;
						String privacy;
						String startTime;
						try {
							JSONObject mjsonObj = new JSONObject(json);
							id = mjsonObj.getString("id");
							timezone = mjsonObj.getString("timezone");
							location = mjsonObj.getString("location");
							desc = mjsonObj.getString("description");
							name = mjsonObj.getString("name");
							privacy = mjsonObj.getString("privacy");
							startTime = mjsonObj
									.getString("start_time");

							fbeventsobj.setId(id);
							fbeventsobj.setTimezone(timezone);
							fbeventsobj.setLocation(location);
							fbeventsobj.setDesc(desc);
							fbeventsobj.setName(name);
							fbeventsobj.setPrivacy(privacy);
							fbeventsobj.setStartTime(startTime);

						} catch (JSONException e) {
							e.printStackTrace();
						}

						eventsObj.add(fbeventsobj);
						//Log.v("EventsObj", eventsObj.size() + "");
						Gson gson = new Gson();
						//								fbEvents = gson.toJson(eventsObj);
						if (arrylength == 0) {
							callbackContext.success(gson
									.toJson(eventsObj));
							//Log.v("fbEvents", gson.toJson(eventsObj)+ "");
						}
					}

				}).executeAsync();

				//Log.v("Events ", eventid + "");
			}

			//Log.v("jarry", jarry + "");

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void sendSMS(String msg) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("sms:"));
		sendIntent.putExtra("sms_body", msg);
		cordova.getActivity().startActivity(sendIntent);
	}

	private void sendEmail(String msg) {
		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_SUBJECT, "WOTNOW");
		email.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(msg));
		email.setType("text/html");
		cordova.getActivity().startActivity(
				Intent.createChooser(email, "Choose an Email client :"));

	}

	// Yahoo Integration

	private static final String CONSUMER_KEY = "dj0yJmk9Znk4ZlNQclR1c015JmQ9WVdrOU5HZzFRMVZxTnpJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02ZA--";
	private static final String CONSUMER_SECRET = "fd8b56bd777f293e01b2b54d3ab28f62cb7834ed";

	private static final String CALLBACK_SCHEME = "http";
	private static final String CALLBACK_HOST = "www.wotnow.me";
	private static final String CALLBACK_URL = CALLBACK_SCHEME + "://"
			+ CALLBACK_HOST;

	private String AUTH_TOKEN = null;
	private String AUTH_TOKEN_SECRET = null;
	private String AUTH_URL = null;
	private String USER_TOKEN = null;
	private String ACCESS_TOKEN = null;
	private String ACCESS_TOKEN_SECRET = null;
	private String mUSER_GUID = null;

	private WebView mWebview;
	private Dialog mDialog;

	SharedPreferences sp;

	Context mContext;

	class getAuthorizationTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			do {
				getAuthorizationToken();
			} while (AUTH_TOKEN == null);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			mDialog = new Dialog(mCordovaActivity);
			 mDialog.setContentView(R.layout.dialog_yahoo_login);
			 mWebview = (WebView) mDialog.findViewById(R.id.webview);

			mWebview.getSettings().setJavaScriptEnabled(true);
			mWebview.setWebViewClient(lWebviewClient);
			mWebview.loadUrl("https://api.login.yahoo.com/oauth/v2/request_auth?oauth_token="
					+ AUTH_TOKEN);
			mDialog.setCancelable(true);
			mDialog.setTitle("Yahoo Login");
			mDialog.show();
		}
	}

	private void getAuthorizationToken() {

		String requestPath = "https://api.login.yahoo.com/oauth/v2/get_request_token?oauth_consumer_key="
				+ CONSUMER_KEY
				+ "&oauth_nonce="
				+ System.currentTimeMillis()
				+ "x"
				+ "&oauth_signature_method=PLAINTEXT"
				+ "&oauth_signature="
				+ CONSUMER_SECRET
				+ "%26"
				+ "&oauth_timestamp="
				+ System.currentTimeMillis()
				+ "&oauth_version=1.0"
				+ "&xoauth_lang_pref=en-us"
				+ "&oauth_callback=" + CALLBACK_URL;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(requestPath);
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			String[] data = responseBody.split("&");
			AUTH_TOKEN = data[0].replace("oauth_token=", "");
			AUTH_TOKEN_SECRET = data[1].replace("oauth_token_secret=", "");
			AUTH_URL = data[3].replace("xoauth_request_auth_url=", "");
			Editor editor = sp.edit();
			editor.putString("AUTH_TOKEN", AUTH_TOKEN);
			editor.putString("AUTH_TOKEN_SECRET", AUTH_TOKEN_SECRET);
			editor.putString("AUTH_URL", AUTH_URL);
			editor.commit();
		} catch (HttpResponseException e) {
			e.printStackTrace();
			getAuthorizationToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	WebViewClient lWebviewClient = new WebViewClient() {

		public void onPageStarted(WebView view, String url,
				android.graphics.Bitmap favicon) {
			System.out.println("SYS URL = " + url);
			if (url.contains("wotnow")) {
				mWebview.stopLoading();
				System.out.println("SYS url called " + url);
				int lastIndex = url.lastIndexOf("=") + 1;
				USER_TOKEN = url.substring(lastIndex, url.length());
				mWebview.setVisibility(View.GONE);

				new getAccessTokenTask().execute();
			}
		};
	};

	class getAccessTokenTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getAccessToken();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

	}

	private void getAccessToken() {
		System.out.println("SYS Access started");
		String requestPath = "https://api.login.yahoo.com/oauth/v2/get_token?oauth_consumer_key="
				+ CONSUMER_KEY
				+ "&oauth_nonce="
				+ System.currentTimeMillis()
				+ "x"
				+ "&oauth_signature_method=PLAINTEXT"
				+ "&oauth_signature="
				+ CONSUMER_SECRET
				+ "%26"
				+ AUTH_TOKEN_SECRET
				+ "&oauth_timestamp="
				+ System.currentTimeMillis()
				+ "&oauth_version=1.0"
				+ "&oauth_token="
				+ AUTH_TOKEN
				+ "&oauth_verifier="
				+ USER_TOKEN;
		System.out.println("SYS " + requestPath);
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(requestPath);
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			String[] data = responseBody.split("&");
			ACCESS_TOKEN = data[0].replace("oauth_token=", "");
			ACCESS_TOKEN_SECRET = data[1].replace("oauth_token_secret=", "");
			mUSER_GUID = data[5].replace("xoauth_yahoo_guid=", "");
		} catch (HttpResponseException e) {
			getAccessToken();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		getProfileInfo();
	}

	private void getProfileInfo() {
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
		String host_url = "https://social.yahooapis.com/v1/user/" + mUSER_GUID
				+ "/profile";

		String nonce = "" + System.currentTimeMillis();
		String timeStamp = "" + (System.currentTimeMillis() / 1000L);

		try {
			String params = "" + encode("oauth_consumer_key") + "="
					+ encode(CONSUMER_KEY) + "&" + encode("oauth_nonce") + "="
					+ encode(nonce) + "&" + encode("oauth_signature_method")
					+ "=" + encode("HMAC-SHA1") + "&"
					+ encode("oauth_timestamp") + "=" + encode(timeStamp) + "&"
					+ encode("oauth_token") + "=" + ACCESS_TOKEN + "&"
					+ encode("oauth_version") + "=" + encode("1.0");
			String baseString = encode("GET") + "&" + encode(host_url) + "&"
					+ encode(params);
			String signingKey = encode(CONSUMER_SECRET) + "&"
					+ encode(ACCESS_TOKEN_SECRET);
			String lSignature = computeHmac(baseString, signingKey);
			lSignature = encode(lSignature);

			String lRequestUrl = host_url + "?oauth_consumer_key="
					+ CONSUMER_KEY + "&oauth_nonce=" + nonce
					+ "&oauth_signature_method=HMAC-SHA1" + "&oauth_timestamp="
					+ timeStamp + "&oauth_token=" + ACCESS_TOKEN
					+ "&oauth_version=1.0" + "&oauth_signature=" + lSignature;
			System.out.println("SYS " + lRequestUrl);
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(lRequestUrl);
			try {
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpclient.execute(httpget,
						responseHandler);
				JSONObject jsonObj = null;
				try {
					jsonObj = XML.toJSONObject(responseBody);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Log.d(TAG, jsonObj.toString());
				savedCallbackContext.success(jsonObj);

			} catch (HttpResponseException e) {
				e.printStackTrace();
				getProfileInfo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Helper Methods

	public String computeHmac(String baseString, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"),
					"HMAC-SHA1");
			mac.init(signingKey);
			byte[] digest = mac.doFinal(baseString.getBytes());
			String result = Base64.encodeToString(digest, Base64.DEFAULT);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String encodeURIComponent(final String value) {
		if (value == null) {
			return "";
		}

		try {
			return URLEncoder.encode(value, "utf-8")
					// OAuth encodes some characters differently:
					.replace("+", "%20").replace("*", "%2A")
					.replace("%7E", "~");
			// This could be done faster with more hand-crafted code.
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String encode(String input) {
		StringBuilder resultStr = new StringBuilder();
		for (char ch : input.toCharArray()) {
			if (isUnsafe(ch)) {
				resultStr.append('%');
				resultStr.append(toHex(ch / 16));
				resultStr.append(toHex(ch % 16));
			} else {
				resultStr.append(ch);
			}
		}
		return resultStr.toString().trim();
	}

	private char toHex(int ch) {
		return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	private boolean isUnsafe(char ch) {
		if (ch > 128 || ch < 0)
			return true;
		return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
	}

	// Google Sign-in

	private GoogleApiClient mGoogleApiClient;
	private CallbackContext savedCallbackContext;
	private boolean loggingOut;
	private boolean GOOGLE_LOGIN = false;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		mGoogleApiClient = buildGoogleApiClient();
	}

	private GoogleApiClient buildGoogleApiClient() {
		return new GoogleApiClient.Builder(webView.getContext())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, Plus.PlusOptions.builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

	private void resolveToken(final String email, final JSONObject result) {
		final Context context = this.cordova.getActivity()
				.getApplicationContext();

		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				String scope = null;
				String token = null;
				try {
					scope = "oauth2:" + Scopes.PLUS_LOGIN;
					token = GoogleAuthUtil.getToken(context, email, scope);
					result.put("oauthToken", token);
				} catch (UserRecoverableAuthException userAuthEx) {
					cordova.getActivity().startActivityForResult(
							userAuthEx.getIntent(), Activity.RESULT_OK);
					return;
				} catch (IOException e) {
					savedCallbackContext.error("Failed to retrieve token: "
							+ e.getMessage());
					return;
				} catch (GoogleAuthException e) {
					savedCallbackContext.error("Failed to retrieve token: "
							+ e.getMessage());
					return;
				} catch (JSONException e) {
					savedCallbackContext.error("Failed to retrieve token: "
							+ e.getMessage());
					return;
				}
				GOOGLE_LOGIN = false;
				Log.d(TAG, result.toString());
				savedCallbackContext.success(result);
			}
		});
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		final Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

		final JSONObject result = new JSONObject();
		try {
			result.put("Email", email);
			if (user != null) {
				result.put("id", user.getId());
				result.put("name", user.getDisplayName());
				result.put("gender", getGender(user.getGender()));
				result.put("isFirstTime", "YES");
				if (user.getImage() != null) {
					result.put("picture", user.getImage().getUrl());
				}
				if (user.getName() != null) {
					result.put("given_name", user.getName().getGivenName());
					result.put("family_name", user.getName().getFamilyName());
				}
			}
			SharedPreferences settings = mCordovaActivity
					.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PREF_ACCOUNT_NAME, email);
			editor.commit();
			resolveToken(email, result);
		} catch (JSONException e) {
			savedCallbackContext.error("result parsing trouble, error: "
					+ e.getMessage());
		}
	}

	private static String getGender(int gender) {
		switch (gender) {
		case 0:
			return "male";
		case 1:
			return "female";
		default:
			return "other";
		}
	}

	@Override
	public void onConnectionSuspended(int constantInClass_ConnectionCallbacks) {
		this.savedCallbackContext.error("connection trouble, code: "
				+ constantInClass_ConnectionCallbacks);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.getErrorCode() == ConnectionResult.SERVICE_MISSING) {
			this.savedCallbackContext.error("service not available");
		} else if (loggingOut) {
			loggingOut = false;
			this.savedCallbackContext.success("logged out");
		} else if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
			final PendingIntent mSignInIntent = result.getResolution();
			try {
				((CordovaActivity) this.cordova.getActivity())
						.setActivityResultCallback(this);
				this.cordova.getActivity().startIntentSenderForResult(
						mSignInIntent.getIntentSender(), 0, null, 0, 0, 0);
			} catch (IntentSender.SendIntentException ignore) {
				mGoogleApiClient.connect();
			}
		} else {
			this.savedCallbackContext.error("no valid token");
		}
	}

	// Google Calendar

	private static final String PREF_ACCOUNT_NAME = "accountName";
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	private Activity mCordovaActivity;

	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	GoogleAccountCredential credential;
	CalendarModel model = new CalendarModel();
	ArrayAdapter<CalendarInfo> adapter;
	com.google.api.services.calendar.Calendar client;
	int numAsyncTasks;

	private void accessGoogleCalendar() {
		mCordovaActivity = this.cordova.getActivity();
		credential = GoogleAccountCredential.usingOAuth2(mContext,
				Collections.singleton(CalendarScopes.CALENDAR));
		SharedPreferences settings = mCordovaActivity.getPreferences(
				Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME,
				null));
		client = new com.google.api.services.calendar.Calendar.Builder(
				transport, jsonFactory, credential).setApplicationName(
				"WotNow/1.0").build();

		if (checkGooglePlayServicesAvailable()) {
			haveGooglePlayServices();
		}
		syncGoogleCalendar(savedCallbackContext);
	}

	// /** Check that Google Play services APK is installed and up to date.
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	private void haveGooglePlayServices() {
		if (credential.getSelectedAccountName() == null) {
//			chooseAccount();
		}
	}

	void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		mCordovaActivity.runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, mCordovaActivity,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	private void chooseAccount() {
		mCordovaActivity.startActivityForResult(
				credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	private void syncGoogleCalendar(final CallbackContext callbackContext) {
		JSONObject allData = null;
		JSONArray googleEvents = null;
		com.google.api.services.calendar.model.Events events;
		try {
			String pageToken = null;
			do {
				events = client
						.events().list("primary").setPageToken(pageToken)
						.execute();
				
				pageToken = events.getNextPageToken();
			} while (pageToken != null);
			try {
				allData = new JSONObject(events.toString());
				googleEvents = allData.getJSONArray("items");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d("Google Events" , googleEvents.toString());
			callbackContext.success(googleEvents);
		} catch (UserRecoverableAuthIOException e) {
			mCordovaActivity.startActivityForResult(e.getIntent(),
					REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (GOOGLE_LOGIN) {
			if (resultCode == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				this.savedCallbackContext.error("user cancelled");
			}
		} else {
			switch (requestCode) {
			case REQUEST_GOOGLE_PLAY_SERVICES:
				if (resultCode == Activity.RESULT_OK) {
					haveGooglePlayServices();
				} else {
					checkGooglePlayServicesAvailable();
				}
				break;
			case REQUEST_AUTHORIZATION:
				if (resultCode != Activity.RESULT_OK) {
					chooseAccount();
				}
				break;
			case REQUEST_ACCOUNT_PICKER:
				if (resultCode == Activity.RESULT_OK && data != null
						&& data.getExtras() != null) {

					String accountName = data.getExtras().getString(
							AccountManager.KEY_ACCOUNT_NAME);
					if (accountName != null) {

						credential.setSelectedAccountName(accountName);
						SharedPreferences settings = mCordovaActivity
								.getPreferences(Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(PREF_ACCOUNT_NAME, accountName);
						editor.commit();
					}
					
				}
				break;
			}
		}
	}
}