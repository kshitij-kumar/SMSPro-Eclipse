package com.kshitij.android.smspro.ui;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.kshitij.android.smspro.R;
import com.kshitij.android.smspro.receiver.SmsReceiver;
import com.kshitij.android.smspro.util.Utility;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * Displays a SMS, either when an item in list is clicked or a notification is
 * clicked
 */
public class SmsViewActivity extends AppCompatActivity {
	private static final String TAG = SmsViewActivity.class.getSimpleName();
	public static final String EXTRA_PHONE_NUMBER = "phone_number";
	public static final String EXTRA_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_view);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		if (getIntent() != null && getIntent().getExtras() != null) {

			int notificationId = getIntent().getExtras().getInt(
					SmsReceiver.EXTRA_NOTIFICATION_ID, -1);
			if (notificationId != -1) {
				NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				manager.cancel(notificationId);
			}
			String phoneNumber = getIntent().getExtras().getString(
					EXTRA_PHONE_NUMBER);
			getSupportActionBar().setTitle(phoneNumber);
			String message = getIntent().getExtras().getString(EXTRA_MESSAGE);
			TextView tvMessage = (TextView) findViewById(R.id.tvMessage);
			tvMessage.setText(message);
			if (Utility.isDeafultSmsApp(this)) {
				Utility.markSmsAsRead(this, phoneNumber, message);
			}
		} else {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
		}

		return super.onOptionsItemSelected(item);
	}
}
