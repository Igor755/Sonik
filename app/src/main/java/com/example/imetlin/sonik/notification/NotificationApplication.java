package com.example.imetlin.sonik.notification;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

public class NotificationApplication extends Application {

	private static final String TAG_NAME = "NotificationService";

	@Override
	public void onCreate() {
		super.onCreate();

		//lets start service to check for new routes
		Intent ns = new Intent(this.getApplicationContext(), NotificationService.class);
		ns.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.getApplicationContext().startService(ns);

		//lets setup checker to check if service is running
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			NotificationServiceCheck.runChecker(this.getApplicationContext());

	}

}
