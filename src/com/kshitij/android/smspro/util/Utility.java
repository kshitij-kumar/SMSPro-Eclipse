package com.kshitij.android.smspro.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

public class Utility {

	public static boolean isNullOrEmpty(String reference) {
		return reference == null || reference.length() == 0;
	}

	public static boolean isDeafultSmsApp(Context context) {

		return Telephony.Sms.getDefaultSmsPackage(context).equals(
				context.getPackageName());
	}

	public static boolean isKnownNumber(String phoneNumber) {
		if (isNullOrEmpty(phoneNumber)) {
			return false;
		} else if (ContentManager.getInstance().getContactsMap()
				.get(phoneNumber).equalsIgnoreCase("Unknown")
				|| isNullOrEmpty(ContentManager.getInstance().getContactsMap()
						.get(phoneNumber))) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Returns contact name for a given number
	 * 
	 * @param context
	 *            the context
	 * @param phoneNumber
	 *            number whose contact names is to be found
	 * @return contact name
	 */
	public static String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}

	/**
	 * Marks an SMS as read
	 */

	public static void markSmsAsRead(final Context context, final String from,
			final String body) {
		if (!isDeafultSmsApp(context))
			return;

		new Thread() {
			@Override
			public void run() {
				String selection = Telephony.Sms.Inbox.ADDRESS + " = ? AND "
						+ Telephony.Sms.Inbox.BODY + " = ? AND "
						+ Telephony.Sms.Inbox.READ + " = ?";
				String[] selectionArgs = { from, body, "0" };

				ContentValues values = new ContentValues();
				values.put(Telephony.Sms.Inbox.READ, true);

				context.getContentResolver().update(
						Telephony.Sms.Inbox.CONTENT_URI, values, selection,
						selectionArgs);
			}
		}.start();
	}

	public static void deleteSms(final Context context,
			final String phoneNumber, final String message) {
		if (!isDeafultSmsApp(context))
			return;

		new Thread() {
			@Override
			public void run() {
				String selection = Telephony.Sms.Inbox.ADDRESS + " = ? AND "
						+ Telephony.Sms.Inbox.BODY + " = ?";
				String[] selectionArgs = { phoneNumber, message };
				context.getContentResolver().delete(Telephony.Sms.CONTENT_URI,
						selection, selectionArgs);
			}
		}.start();
	}

	/* Save sent message */
	public static void saveSentSms(final Context context,
			final String phoneNumber, final String message) {
		if (!isDeafultSmsApp(context))
			return;

		new Thread() {
			@Override
			public void run() {
				ContentValues values = new ContentValues();
				values.put(Telephony.Sms.Sent.ADDRESS, phoneNumber);
				values.put(Telephony.Sms.Sent.BODY, message);
				context.getContentResolver().insert(
						Telephony.Sms.Sent.CONTENT_URI, values);
			}
		}.start();
	}

	/* Saves a received message in inbox, marked as unread */
	public static void saveReceivedSms(final Context context,
			final String phoneNumber, final String message) {
		if (!isDeafultSmsApp(context))
			return;

		new Thread() {
			@Override
			public void run() {
				ContentValues values = new ContentValues();
				values.put(Telephony.Sms.Inbox.ADDRESS, phoneNumber);
				values.put(Telephony.Sms.Inbox.BODY, message);
				values.put(Telephony.Sms.Inbox.READ, 0);
				context.getContentResolver().insert(
						Telephony.Sms.Inbox.CONTENT_URI, values);
			}
		}.start();
	}
}
