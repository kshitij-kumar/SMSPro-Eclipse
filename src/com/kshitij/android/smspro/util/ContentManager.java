package com.kshitij.android.smspro.util;

import java.util.Map;

/**
 * Created by kshitij.kumar on 30-06-2015.
 */

/**
 * A helper class to maintain map of contact number to their names
 * 
 */
public class ContentManager {
	public static final String TAG = ContentManager.class.getSimpleName();
	static ContentManager mContentManager;
	Map<String, String> mContactsMap;

	public synchronized static ContentManager getInstance() {
		if (mContentManager == null) {
			mContentManager = new ContentManager();
		}
		return mContentManager;
	}

	public Map<String, String> getContactsMap() {
		return mContactsMap;
	}

	public void setContactsMap(Map<String, String> contactsMap) {
		this.mContactsMap = contactsMap;
	}

}
