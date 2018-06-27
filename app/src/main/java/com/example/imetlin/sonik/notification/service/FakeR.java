package com.example.imetlin.sonik.notification.service;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

/**
 * R replacement for PhoneGap Build.
 *
 * ([^.\w])R\.(\w+)\.(\w+)
 * $1fakeR("$2", "$3")
 *
 */
public class FakeR {
	private Context context;
	private String packageName;

	public FakeR(Activity activity) {
		context = activity.getApplicationContext();
		packageName = context.getPackageName();
	}

	public FakeR(Context context) {
		this.context = context;
		packageName = context.getPackageName();
	}

	public int getId(String group, String key) {
		return context.getResources().getIdentifier(key, group, packageName);
	}

	public static int getId(Context context, String group, String key) {
		return context.getResources().getIdentifier(key, group, context.getPackageName());
	}

	public String byIdName(String name) {
		Resources res = context.getResources();
		return context.getResources().getString(res.getIdentifier(name, "string", context.getPackageName()));
	}
}
