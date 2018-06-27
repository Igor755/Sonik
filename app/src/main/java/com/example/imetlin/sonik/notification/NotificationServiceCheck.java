/*
 * This is a scheduled task to check if our service is still running 
 */

package com.example.imetlin.sonik.notification;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

@SuppressLint("NewApi")
public class NotificationServiceCheck extends JobService {

	private static final String TAG_NAME = "NotificationService";
	
	private static boolean status;
	
	public static void runChecker(Context context) {		
		Log.d(TAG_NAME, "Setup schedule to check for service status");
		ComponentName mServiceComponent = new ComponentName(context, NotificationServiceCheck.class);
		JobInfo.Builder builder = new JobInfo.Builder(new Random().nextInt(), mServiceComponent);
		builder.setPersisted(true);
		builder.setPeriodic(60 * 1000);
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		jobScheduler.schedule(builder.build());
		
		//receive reply back
		IntentFilter filter = new IntentFilter("SERVICE_RUNNING_OK");
		context.registerReceiver(receiver, filter);
	}

	@SuppressLint("Override")
	@Override
	public boolean onStartJob(JobParameters params) {
		//send intent to check if service is running
		Log.d(TAG_NAME, "Send intent to check if service is running: SERVICE_RUNNING");
		
		status = false;
		
		//send
		Intent intent = new Intent("SERVICE_RUNNING");
		intent.setPackage(getApplicationContext().getPackageName());
		getApplicationContext().sendBroadcast(intent);		
		
		//check service in 5 seks
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Log.d(TAG_NAME, "Service is running: " + status);
				if (status == false) {
					Intent uc = new Intent(getApplicationContext(), com.example.imetlin.sonik.notification.NotificationService.class);
					uc.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplicationContext().startService(uc);
				} 
			}
		}, 5000);
		
		return true;
	}

	@SuppressLint("Override")
	@Override
	public boolean onStopJob(JobParameters params) {
		return true;
	}
	
	private final static BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG_NAME, "Received intent: " + intent.getAction());
			status = true; //service running
		}
	};

}
