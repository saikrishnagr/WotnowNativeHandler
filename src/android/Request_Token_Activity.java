package com.google.profile;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpResponse;
import oauth.signpost.signature.HmacSha1MessageSigner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Request_Token_Activity extends Activity {
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "https://api.login.yahoo.com/oauth/v2/get_request_token";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "https://api.login.yahoo.com/oauth/v2/get_access_token";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api.login.yahoo.com/oauth/v2/request_auth";
	private static final int PIN_DIALOG = 0;
	String CALLBACK_URL = OAuth.OUT_OF_BAND; // this should be the same as the
	// SCHEME and HOST values in
	// your AndroidManifest.xml file
	String CONSUMER_KEY = "dj0yJmk9Znk4ZlNQclR1c015JmQ9WVdrOU5HZzFRMVZxTnpJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02ZA--";//
	String CONSUMER_SECRET = "fd8b56bd777f293e01b2b54d3ab28f62cb7834ed";
	private CommonsHttpOAuthConsumer myConsumer;
	private CommonsHttpOAuthProvider myProvider;
	private String requestToken;
	private String accessToken;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		callOAuth();
		showDialog(PIN_DIALOG);
		// createPinDialog().show();
	}

	private void callOAuth() {
		try {
			// retrieve the consumer token and then sign it
			myConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
					CONSUMER_SECRET);

			myConsumer.setMessageSigner(new HmacSha1MessageSigner());

			HttpClient client = new DefaultHttpClient();
			// retrieve the provider by using the signed consumer token
			myProvider = new CommonsHttpOAuthProvider(
					REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
					AUTHORIZE_WEBSITE_URL, client);
			myProvider.setOAuth10a(true);
			String aUrl = myProvider.retrieveRequestToken(myConsumer,
					CALLBACK_URL);

			requestToken = myConsumer.getToken();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(aUrl)));
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(ex.getMessage(), ex.toString());
		}
	}

	// this is the callback function that will run when oauth authenticates
	// successfully
	@Override
	protected void onNewIntent(Intent intent) {
		System.out.println("OnNewIntent...");
		Toast.makeText(getApplicationContext(), "OnNewIntent - It works!",
				Toast.LENGTH_LONG).show();
		// whatever you want to do after authenticating goes here ....
	}

//	AlertDialog createPinDialoga() {
//		LayoutInflater factory = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//
//		// LayoutInflater factory = LayoutInflater.from(this);
//		final View textEntryView = factory.inflate(R.layout.pin, null);
//		final EditText pinText = (EditText) textEntryView
//				.findViewById(R.id.pin_text);
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle("Twitter OAuth PIN");
//		builder.setView(textEntryView);
//		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int whichButton) {
//				if (pinText != null)
//					gotOAuthPin(pinText.getText().toString());
//				onResume();
//			}
//		});
//		return builder.create();
//	}

	private void gotOAuthPin(String pin) {
		SharedPreferences.Editor editor = getSharedPreferences("yahoo",
				MODE_PRIVATE).edit();
		try {
			myProvider.retrieveAccessToken(myConsumer, pin);
			accessToken = myConsumer.getToken();

		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (accessToken != null && accessToken.length() > 0) {
			Toast.makeText(this, "Authorized", Toast.LENGTH_SHORT).show();
			HttpPost request = new HttpPost(
					"http://social.yahooapis.com/v1/user/profile?format=json");
			StringEntity body = null;
			/*
			 * try { body = new StringEntity("city=hamburg&label=" +
			 * URLEncoder.encode("Send via Signpost!", "UTF-8")); } catch
			 * (UnsupportedEncodingException e1) { // TODO Auto-generated catch
			 * block e1.printStackTrace(); }
			 * body.setContentType("application/x-www-form-urlencoded");
			 * request.setEntity(body);
			 */

			try {
				myConsumer.sign(request);
			} catch (OAuthMessageSignerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthExpectationFailedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthCommunicationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("Sending update request to Fire Eagle...");

			HttpClient httpClient = new DefaultHttpClient();
			org.apache.http.HttpResponse response = null;
			try {
				response = httpClient.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Toast.makeText(
					this,
					"Response: " + response.getStatusLine().getStatusCode()
							+ " " + response.getStatusLine().getReasonPhrase(),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Not Authorized", Toast.LENGTH_SHORT).show();
		}
	}

//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case PIN_DIALOG:
//			LayoutInflater factory = LayoutInflater.from(this);
//			final View textEntryView = factory.inflate(R.layout.pin, null);
//			final EditText pinText = (EditText) textEntryView
//					.findViewById(R.id.pin_text);
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("OAuth PIN");
//			builder.setView(textEntryView);
//			builder.setPositiveButton("OK",
//					new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog,
//								int whichButton) {
//							if (pinText != null)
//								gotOAuthPin(pinText.getText().toString());
//						}
//					});
//			return builder.create();
//		}
//
//		return super.onCreateDialog(id);
//	}
}