package com.kshitij.android.smspro.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.util.Log;

import com.kshitij.android.smspro.R;
import com.kshitij.android.smspro.ui.SmsViewActivity;
import com.kshitij.android.smspro.util.Utility;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * An implementation of BroadcastReceiver, used to receive SMS
 */

public class SmsReceiver extends BroadcastReceiver {

	private static final String TAG = SmsReceiver.class.getSimpleName();
	private static final String SMS_BUNDLE = "pdus";
	public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		Bundle intentExtras = intent.getExtras();
		if (intentExtras != null) {
			Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
			for (int i = 0; i < sms.length; ++i) {
				SmsMessage smsMessage = SmsMessage
						.createFromPdu((byte[]) sms[i]);

				String message = smsMessage.getMessageBody().toString();
				String phoneNumber = smsMessage.getOriginatingAddress();
				if (Utility.isDeafultSmsApp(context)) {
					saveMessage(context, phoneNumber, message);
				}
				displayNotification(context, phoneNumber, message);
			}
		}
	}

	/* Saves a received message in inbox, marked as unread */
	private void saveMessage(Context context, String phoneNumber, String message) {
		ContentValues values = new ContentValues();
		values.put(Telephony.Sms.Inbox.ADDRESS, phoneNumber);
		values.put(Telephony.Sms.Inbox.BODY, message);
		values.put(Telephony.Sms.Inbox.READ, 0);
		context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI,
				values);
	}

	/*
	 * Builds a notification, on clicking notification SmsViewActivity is
	 * launched
	 */
	private void displayNotification(Context context, String phoneNumber,
			String message) {
		int notificationId = 007;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(phoneNumber).setContentText(message);

		Intent viewIntent = new Intent(context, SmsViewActivity.class);
		viewIntent.putExtra(SmsViewActivity.EXTRA_PHONE_NUMBER, phoneNumber);
		viewIntent.putExtra(SmsViewActivity.EXTRA_MESSAGE, message);
		viewIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(SmsViewActivity.class);

		stackBuilder.addNextIntent(viewIntent);
		PendingIntent viewPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(viewPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
	}
}