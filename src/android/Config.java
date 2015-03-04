package com.alltivity.wotnow;

public interface Config {

	
	// CONSTANTS
	static final String YOUR_SERVER_URL ="http://172.16.26.196/wotnow_svn/wotnow/androidpns/apns.php";
    // Google project id  - 202223809592  mine - 543283526722
    static final String GOOGLE_SENDER_ID = "492698376031"; 
    /**
     * Tag used on log messages.
     */
    static final String TAG = "WotNow";

    static final String DISPLAY_MESSAGE_ACTION =
            "com.google.profile.DISPLAY_MESSAGE";

    static final String EXTRA_MESSAGE = "message";
		
	
}