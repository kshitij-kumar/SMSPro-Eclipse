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

	public static void markSmsAsRead(Context context, String from, String body) {

		String selection = "address = ? AND body = ? AND read = ?";
		String[] selectionArgs = { from, body, "0" };

		ContentValues values = new ContentValues();
		values.put("read", true);

		context.getContentResolver().update(Telephony.Sms.Inbox.CONTENT_URI,
				values, selection, selectionArgs);
	}

	public static void deleteSms(Context context, String phoneNumber,
			String message) {
		String selection = "address = ? AND body = ?";
		String[] selectionArgs = { phoneNumber, message };
		context.getContentResolver().delete(Telephony.Sms.CONTENT_URI,
				selection, selectionArgs);
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
}
