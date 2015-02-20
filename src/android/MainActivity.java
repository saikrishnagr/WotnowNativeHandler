package com.google.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	// label to display gcm messages
	TextView lblMessage;
	HMSHostApplication aController;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;

	public static String name;
	public static String email;
	private static String registrationId;

	@Override
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.gcmmain);
         
        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        aController = (HMSHostApplication) getApplicationContext();
         
         
        // Check if Internet present
        if (!aController.isConnectingToInternet()) {
             
            // Internet Connection is not present
            aController.showAlertDialog(MainActivity.this,
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
 
//        lblMessage = (TextView) findViewById(R.id.lblMessage);
         
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
						
							lblMessage.setText("GCM reg id of device - " + GCMRegistrar.getRegistrationId(MainActivity.this));
							registrationId = lblMessage.getTag().toString();//GCMRegistrar.getRegistrationId(MainActivity.this);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			});
             
        } else {
        	  // Skips registration.              
            Toast.makeText(getApplicationContext(), 
                          regId, 
                          Toast.LENGTH_LONG).
                          show();
            Log.v("gcm reg id", regId);
            registrationId = regId;
            lblMessage.setText("GCM reg id of device - " + regId);
            
            // Device is already registered on GCM Server
           /* if (GCMRegistrar.isRegisteredOnServer(this)) {
                 
                // Skips registration.              
                Toast.makeText(getApplicationContext(), 
                              "Already registered with GCM Server", 
                              Toast.LENGTH_LONG).
                              show();
             
            } else {
                 
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                 
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
                    @Override
                    protected Void doInBackground(Void... params) {
                         
                        // Register on our server
                        // On server creates a new user
                        aController.register(context, name, email, regId);
                         
                        return null;
                    }
 
                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }
 
                };
                 
                // execute AsyncTask
                mRegisterTask.execute(null, null, null);
            }*/
        }
    }	// Create a broadcast receiver to get message and show on screen
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String newMessage = intent.getExtras().getString(
					Config.EXTRA_MESSAGE);

			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());

			// Display message on the screen
			lblMessage.append(newMessage + "");

			Toast.makeText(getApplicationContext(),	"Got Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};

	@Override
	protected void onDestroy() {
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

}