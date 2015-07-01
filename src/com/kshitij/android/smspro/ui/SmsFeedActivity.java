package com.kshitij.android.smspro.ui;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.kshitij.android.smspro.R;
import com.kshitij.android.smspro.adapter.SmsFeedAdapter;
import com.kshitij.android.smspro.util.ContentManager;
import com.kshitij.android.smspro.util.Utility;
import com.melnykov.fab.FloatingActionButton;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * Display list of all messages, uses Loaders to retrieve data from
 * ContentProvider
 */

public class SmsFeedActivity extends AppCompatActivity implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = SmsFeedActivity.class.getSimpleName();
	private Context mContext;
	private ListView mSmsListView;
	private SmsFeedAdapter mSmsFeedAdapter;
	private ProgressDialog mProgressDialog;
	private ContactIdentificationTask mContactIdentificationTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_sms_feed);
		mSmsListView = (ListView) findViewById(R.id.SMSList);
		mSmsFeedAdapter = new SmsFeedAdapter(this, null, false);
		mSmsListView.setAdapter(mSmsFeedAdapter);

		// Dismiss functionality
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				mSmsListView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int index : reverseSortedPositions) {
							Cursor cursor = mSmsFeedAdapter.getCursor();
							cursor.moveToPosition(index);
							String phoneNumber = cursor.getString(cursor
									.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
							String message = cursor.getString(cursor
									.getColumnIndexOrThrow(Telephony.Sms.BODY));
							if (Utility.isKnownNumber(phoneNumber)) {
								Utility.markSmsAsRead(mContext, phoneNumber,
										message);
							} else {
								Utility.deleteSms(mContext, phoneNumber,
										message);
							}
							mSmsFeedAdapter.notifyDataSetChanged();
						}
					}
				});

		// Click item to view message
		mSmsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				viewMessage(position);
			}
		});

		// Click fab to compose new message
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.attachToListView(mSmsListView);
		fab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent composeSmsIntent = new Intent(mContext,
						SmsComposeActivity.class);
				startActivity(composeSmsIntent);
			}
		});

		mSmsListView.setOnTouchListener(touchListener);
		mSmsListView.setOnScrollListener(touchListener.makeScrollListener());

		getSupportLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(TAG, "onCreateLoader()");
		CursorLoader loader = new CursorLoader(this, Telephony.Sms.CONTENT_URI,
				new String[] {}, null, null, Telephony.Sms.DEFAULT_SORT_ORDER);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (ContentManager.getInstance().getContactsMap() == null) {
			// Map contacts to names
			mContactIdentificationTask = new ContactIdentificationTask();
			mContactIdentificationTask.execute(cursor);
		} else {
			mSmsFeedAdapter.swapCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mSmsFeedAdapter.swapCursor(null);
	}

	private void viewMessage(int position) {
		Cursor cursor = mSmsFeedAdapter.getCursor();
		cursor.moveToPosition(position);
		String phoneNumber = cursor.getString(cursor
				.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
		String message = cursor.getString(cursor
				.getColumnIndexOrThrow(Telephony.Sms.BODY));
		Log.d(TAG, "viewMessage(), Message = " + message);
		Intent viewIntent = new Intent(this, SmsViewActivity.class);
		viewIntent.putExtra(SmsViewActivity.EXTRA_PHONE_NUMBER, phoneNumber);
		viewIntent.putExtra(SmsViewActivity.EXTRA_MESSAGE, message);
		startActivity(viewIntent);

	}

	private void dismissLoadingProgress() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	private void showLoadingProgress() {
		dismissLoadingProgress();
		mProgressDialog = ProgressDialog.show(this, "", "Loading...", true,
				false);
	}

	/*
	 * An Async task which map contact numbers to names, it takes less time than
	 * expected
	 */
	private class ContactIdentificationTask extends
			AsyncTask<Cursor, Void, Cursor> {
		@Override
		protected void onPreExecute() {
			Log.d(TAG, "ContactIdentificationTask, onPreExecute()");
			showLoadingProgress();
			super.onPreExecute();
		}

		@Override
		protected Cursor doInBackground(Cursor... params) {
			Log.d(TAG, "ContactIdentificationTask, doInBackground()");
			Cursor cursor = params[0];
			Map<String, String> contactsMap = new HashMap<String, String>();
			for (int index = 0; index < cursor.getCount(); index++) {
				cursor.moveToPosition(index);
				String phoneNumber = cursor.getString(cursor
						.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
				if (contactsMap.get(phoneNumber) == null) {
					String name = Utility.getContactName(mContext, phoneNumber);
					if (Utility.isNullOrEmpty(name)) {
						contactsMap.put(phoneNumber, "Unknown");
					} else {
						contactsMap.put(phoneNumber, name);
					}
				}
			}
			ContentManager.getInstance().setContactsMap(contactsMap);
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			Log.d(TAG, "ContactIdentificationTask, onPostExecute()");
			dismissLoadingProgress();
			mSmsFeedAdapter.swapCursor(result);
			super.onPostExecute(result);
		}
	}
}
