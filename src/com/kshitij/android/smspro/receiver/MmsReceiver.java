package com.kshitij.android.smspro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * Dummy MMS receiver to make sure this app can be default SMS app
 */

public class MmsReceiver extends BroadcastReceiver {

	private static final String TAG = MmsReceiver.class.getSimpleName();

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive(), MMS Message Received");
	}
}