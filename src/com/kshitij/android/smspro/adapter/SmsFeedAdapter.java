package com.kshitij.android.smspro.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.Telephony;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kshitij.android.smspro.R;
import com.kshitij.android.smspro.util.ContentManager;
import com.kshitij.android.smspro.util.TimeFormatter;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * An implementation of CursorAdapter, used to display sms feed
 */

public class SmsFeedAdapter extends CursorAdapter {

	private LayoutInflater mInflater;

	public SmsFeedAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		// Use View-Holder pattern to avoid redundant findViewById calls
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		if (viewHolder == null) {
			viewHolder = new ViewHolder();
			viewHolder.mSmsIcon = (ImageView) convertView
					.findViewById(R.id.sms_icon);
			viewHolder.mTxtPersonName = (TextView) convertView
					.findViewById(R.id.tvPersonName);
			viewHolder.mTxtSmsSummary = (TextView) convertView
					.findViewById(R.id.tvSmsSummary);
			viewHolder.mTxtSmsTime = (TextView) convertView
					.findViewById(R.id.tvSmsTime);
			convertView.setTag(viewHolder);
		}

		// Read values from cursor and populate fields of SMS feed item
		String readStatus = cursor.getString(cursor
				.getColumnIndexOrThrow(Telephony.Sms.READ));

		if (readStatus.equalsIgnoreCase("0")) {
			viewHolder.mTxtPersonName.setTypeface(null, Typeface.BOLD);
			viewHolder.mTxtSmsSummary.setTypeface(null, Typeface.BOLD);
			viewHolder.mTxtSmsTime.setTypeface(null, Typeface.BOLD);
		} else {
			viewHolder.mTxtPersonName.setTypeface(null, Typeface.NORMAL);
			viewHolder.mTxtSmsSummary.setTypeface(null, Typeface.NORMAL);
			viewHolder.mTxtSmsTime.setTypeface(null, Typeface.NORMAL);
		}

		String phoneNumber = cursor.getString(cursor
				.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
		if (ContentManager.getInstance().getContactsMap() != null
				&& ContentManager.getInstance().getContactsMap()
						.get(phoneNumber) != null
				&& !ContentManager.getInstance().getContactsMap()
						.get(phoneNumber).equalsIgnoreCase("Unknown")) {
			viewHolder.mTxtPersonName.setText(ContentManager.getInstance()
					.getContactsMap().get(phoneNumber));
			convertView.setBackgroundColor(context.getResources().getColor(
					R.color.light_blue));
		} else {
			viewHolder.mTxtPersonName.setText(phoneNumber);
			convertView.setBackgroundColor(context.getResources().getColor(
					(android.R.color.white)));
		}
		viewHolder.mTxtSmsSummary.setText(cursor.getString(cursor
				.getColumnIndexOrThrow(Telephony.Sms.BODY)));
		viewHolder.mTxtSmsTime.setText(TimeFormatter
				.getCustomisedTimeLabel(Long.parseLong(cursor.getString(cursor
						.getColumnIndexOrThrow(Telephony.Sms.DATE)))));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View rowView = mInflater.inflate(R.layout.sms_feed_item, parent, false);

		return rowView;
	}

	public static class ViewHolder {
		ImageView mSmsIcon;
		public TextView mTxtPersonName;
		TextView mTxtSmsSummary;
		TextView mTxtSmsTime;
	}
}