package com.alltivity.wotnow;
 
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
 
public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String TAG = "GCMIntentService";
     
    private WotNowApplication aController = null;
    public static String Url;
 
    public GCMIntentService() {
        // Call extended class Constructor GCMBaseIntentService
        super(Config.GOOGLE_SENDER_ID);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
         
        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        if(aController == null)
           aController = (WotNowApplication) getApplicationContext();
         
        Log.d(TAG, "Device registered: regId = " + registrationId);
        aController.displayMessageOnScreen(context,"Your device registred with GCM id - " + registrationId);
      //  Log.d("NAME", MainActivity.name);
      /*  aController.register(context, MainActivity.name, MainActivity.email, registrationId);*/
    }
 
    /**
     * Method called on device unregistred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if(aController == null)
            aController = (WotNowApplication) getApplicationContext();
        Log.d(TAG, "Device unregistered");
        aController.displayMessageOnScreen(context, 
                                            getString(R.string.gcm_unregistered));
        aController.unregister(context, registrationId);
    }
 
    /**
     * Method called on Receiving a new message from GCM server
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
         
        if(aController == null)
            aController = (WotNowApplication) getApplicationContext();
         
        
        String message = intent.getExtras().getString("message");
        Log.d(TAG, "Received message : " + message);
        String url = intent.getExtras().getString("url");
//        //Log.v(TAG, url);
        aController.displayMessageOnScreen(context, message);
        // notifies user
        generateNotification(context, message, url);
    }
 
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
         
        if(aController == null)
            aController = (WotNowApplication) getApplicationContext();
         
        Log.d(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        aController.displayMessageOnScreen(context, message);
        // notifies user
        generateNotification(context, message, "");
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
         
        if(aController == null)
            aController = (WotNowApplication) getApplicationContext();
         
        Log.d(TAG, "Received error: " + errorId);
        aController.displayMessageOnScreen(context, 
                                   getString(R.string.gcm_error, errorId));
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
         
        if(aController == null)
            aController = (WotNowApplication) getApplicationContext();
         
        // log message
        Log.d(TAG, "Received recoverable error: " + errorId);
        aController.displayMessageOnScreen(context, 
                        getString(R.string.gcm_recoverable_error,
                        errorId));
        return super.onRecoverableError(context, errorId);
    }
 
    /**
     * Create a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
	private static void generateNotification(Context context, String message, String url) {
    	String msg = null;
    	int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Log.d("generateNotification---", message);
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        /*try {
			@SuppressWarnings("deprecation")
			JSONObject mJsonObject = new JSONObject(message);
			 msg = mJsonObject.getString("message");
			 Url = mJsonObject.getString("url");
			 //Log.v("Url--->", Url);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
		Notification notification = new Notification(icon, message, when);
         
        String title = context.getString(R.string.app_name);
         
        Intent notificationIntent = new Intent(context, FirstPage.class);
       // String Url = "Url:" + url;
        notificationIntent.putExtra("KEY", "somthing");
        notificationIntent.putExtra("URL", url);
       /* notificationIntent.putExtra("Url", url);*/
        // set intent so it does not start a new activity
   /*     notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);*/
       /* notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        context.startActivity(notificationIntent);*/
        
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, msg, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
         
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
         
       /* //notification.sound = Uri.parse(
                               "android.resource://"
                               + context.getPackageName() 
                               + "your_sound_file_name.mp3");*/
         
        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);      
 
    }
 
}