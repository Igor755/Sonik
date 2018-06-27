/*
 * Finder service responsible for loading additional jar file with classes to make request to the server
 * Also makes requests every 10 mins to check for new routes
 */

package com.example.imetlin.sonik.notification;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationService extends Service {

	private static String TAG_NAME = "NotificationService";

	private Context context;	

	private boolean status = false; //status of NS

	@Override
	public void onCreate() {

		this.context = this;

		Log.d(TAG_NAME, "Create new service");

		//filter to reply back from service when it is checked so we know it is running
		IntentFilter filter = new IntentFilter("SERVICE_RUNNING");
		context.registerReceiver(receiveServiceStatus, filter);

		//register NS verifier
		IntentFilter filter2 = new IntentFilter("RCNS");
		context.registerReceiver(receiver, filter2);

		Intent intent = new Intent("CNS");
		intent.setPackage(context.getPackageName());
		context.sendBroadcast(intent);

		//read from shared if we just installed it
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String installedOn = sharedPreferences.getString("installedOn", null);
		//if we do not have installed date saved yet means we just installed it
		if (installedOn == null) {
			Editor shared = sharedPreferences.edit();
			shared.putString("installedOn", String.valueOf(System.currentTimeMillis()));
			shared.commit();
		}

		//check service in 5 seks
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Log.d(TAG_NAME, "NS status: " + status);
				if (status == false) {
					Log.d(TAG_NAME, "Launching NS");
					new com.example.imetlin.sonik.notification.service.NotificationService().create(new Object[] { context, context });
				}
				context.unregisterReceiver(receiver); //remove receiver
			}
		}, 5000);

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	//receives intent when checking for service running
	private final BroadcastReceiver receiveServiceStatus = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG_NAME, "Received intent isServiceRunning ? send reply back");

			//send
			Intent intent2 = new Intent("SERVICE_RUNNING_OK");
			intent2.setPackage(context.getPackageName());
			getApplicationContext().sendBroadcast(intent2);
		}
	};

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG_NAME, "Received intent: " + intent.getAction());
			status = true; //service running
		}
	};

}
