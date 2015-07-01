package com.kshitij.android.smspro.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kshitij.android.smspro.R;
import com.kshitij.android.smspro.util.ContentManager;
import com.kshitij.android.smspro.util.Utility;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * Composes SMS and sends to recipient
 */
public class SmsComposeActivity extends AppCompatActivity {
	private static final int PICK_CONTACT_REQUEST = 1001;
	private static final String SENT = "SMS_SENT";
	public static final String DELIVERED = "SMS_DELIVERED";
	private EditText mEdtTxtRecipient;
	private ImageView mImgPickContact;
	private EditText mEdtTxtMessage;
	private ImageView mImgSend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_compose);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mEdtTxtRecipient = (EditText) findViewById(R.id.edtTxtRecipient);
		mImgPickContact = (ImageView) findViewById(R.id.ivPickContact);
		mEdtTxtMessage = (EditText) findViewById(R.id.edtTxtMessage);
		mImgSend = (ImageView) findViewById(R.id.ivSend);
		mImgSend.setEnabled(false);

		/* Use contact picker to choose from stored contact */
		mImgPickContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideKeyboard();
				pickContact();
			}
		});

		/* Send button is enabled only when there is text to be sent */
		mEdtTxtMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				controlSendButton(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		/* Validate and take action accordingly */
		mImgSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String phoneNo = mEdtTxtRecipient.getText().toString();
				String message = mEdtTxtMessage.getText().toString();
				if (phoneNo.length() > 0) {
					mImgSend.setEnabled(false);
					hideKeyboard();
					sendSMS(phoneNo, message);
				} else if (mEdtTxtRecipient.length() == 0) {
					mEdtTxtRecipient.requestFocus();
					mEdtTxtRecipient
							.setError(getString(R.string.error_add_recipient));
				} else {
					Toast.makeText(getBaseContext(), "Invalid recipient.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_CONTACT_REQUEST) {
			if (resultCode == RESULT_OK) { // Contact has been chosen
				Uri contactUri = data.getData();
				String[] projection = { Phone.NUMBER, Phone.DISPLAY_NAME };
				Cursor cursor = getContentResolver().query(contactUri,
						projection, null, null, null);
				cursor.moveToFirst();
				String phoneNo = cursor.getString(cursor
						.getColumnIndex(Phone.NUMBER));
				String name = cursor.getString(cursor
						.getColumnIndex(Phone.DISPLAY_NAME));
				identifyContact(phoneNo, name);
				mEdtTxtRecipient.setText(phoneNo);
				mEdtTxtMessage.requestFocus();
			}
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

	/* Launches contact picker */
	private void pickContact() {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
				Uri.parse("content://contacts"));
		pickContactIntent.setType(Phone.CONTENT_TYPE);
		startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
	}

	private void controlSendButton(CharSequence s) {
		if (s.length() == 0) {
			mImgSend.setImageResource(R.drawable.ic_send_disabled);
			mImgSend.setEnabled(false);
		} else {
			mImgSend.setImageResource(R.drawable.ic_send);
			mImgSend.setEnabled(true);
		}
	}

	private void hideKeyboard() {

		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private void identifyContact(String phoneNo, String name) {
		ContentManager.getInstance().getContactsMap().put(phoneNo, name);
	}

	/* Send message using SmsManager */
	private void sendSMS(String phoneNumber, String message) {

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "Sent", Toast.LENGTH_SHORT)
							.show();
					finish();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Failed",
							Toast.LENGTH_SHORT).show();
					mImgSend.setEnabled(true);
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "Failed",
							Toast.LENGTH_SHORT).show();
					mImgSend.setEnabled(true);
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Failed",
							Toast.LENGTH_SHORT).show();
					mImgSend.setEnabled(true);
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Failed",
							Toast.LENGTH_SHORT).show();
					mImgSend.setEnabled(true);
					break;
				}
			}
		}, new IntentFilter(SENT));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
		if (Utility.isDeafultSmsApp(this)) {
			saveMessage(this, phoneNumber, message);
		}
	}

	/* Save sent message */
	public static void saveMessage(Context context, String phoneNumber,
			String message) {
		ContentValues values = new ContentValues();
		values.put(Telephony.Sms.Sent.ADDRESS, phoneNumber);
		values.put(Telephony.Sms.Sent.BODY, message);
		context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI,
				values);
	}
}