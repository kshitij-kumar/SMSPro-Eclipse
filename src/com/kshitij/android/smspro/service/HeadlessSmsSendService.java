package com.kshitij.android.smspro.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * Dummy service to make sure this app can be default SMS app
 */
public class HeadlessSmsSendService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}