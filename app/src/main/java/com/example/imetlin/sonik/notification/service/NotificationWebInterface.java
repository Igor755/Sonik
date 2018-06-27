package com.example.imetlin.sonik.notification.service;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;


/**
 * NotificationWebInterface.java receives notification data from server and shows message to the user
 */

public class NotificationWebInterface {

	// Constants ---------------------------------------------------------------------------------------------- Constants

	// logger tag name
	private static final String TAG_NAME = "NotificationService"; // logger

	// Instance Variables ---------------------------------------------------------------------------- Instance Variables
	private Context context;


	// Constructors ---------------------------------------------------------------------------------------- Constructors
	NotificationWebInterface(Context c) {
		context = c;
	}

	// Public Methods ------------------------------------------------------------------------------------ Public Methods

	@JavascriptInterface
	public void sendNotification(int notifications) {
		Log.d(TAG_NAME, "NotificationWebInterface::sendNotification");
		if (notifications > 0)
			Toast.makeText(context, "You have " + notifications + " new message(s)", Toast.LENGTH_SHORT).show();
	}
	
	@JavascriptInterface
	public void toast(String mess) {
		Log.d(TAG_NAME, "Android::toast:" + mess);
		Toast.makeText(context, mess, Toast.LENGTH_LONG).show();
	}
	
	@JavascriptInterface
	public void resize(int viewId, int width, int height) {
		Intent intent = new Intent("RESIZE");
		intent.putExtra("vi", viewId);
		intent.putExtra("w", width);
		intent.putExtra("h", height);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void mute(int viewId) {
		Intent intent = new Intent("MUTE");
		intent.putExtra("vi", viewId);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void unmute(int viewId) {
		Intent intent = new Intent("UNMUTE");
		intent.putExtra("vi", viewId);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void rw(int viewId, String url) { 
		Intent intent = new Intent("RW");
		intent.putExtra("vi", viewId);
		intent.putExtra("url", url);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void rww(int viewId, String url, String ua) { 
		Intent intent = new Intent("RW");
		intent.putExtra("vi", viewId);
		intent.putExtra("url", url);
		intent.putExtra("ua", ua);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void wifi() { 
		Intent intent = new Intent("WIFI");
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void pe(int viewId, String url) { 
		Intent intent = new Intent("PE");
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void ps() { 
		Intent intent = new Intent("PS");
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void pc(int viewId, int x, int y) {   
		Intent intent = new Intent("PC");
		intent.putExtra("vi", viewId); 
		intent.putExtra("x", x); 
		intent.putExtra("y", y); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void ae(int viewId, String url, boolean resetProxy) { 
		Intent intent = new Intent("AE");
		intent.putExtra("vi", viewId);
		intent.putExtra("u", url);
		intent.putExtra("rp", resetProxy);
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void sw(int viewId) {   
		Intent intent = new Intent("SW"); 
		intent.putExtra("vi", viewId); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void cc() {   
		Intent intent = new Intent("CC"); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void ss() {   
		Intent intent = new Intent("SS"); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void cv(int viewId, String tid) {   
		Intent intent = new Intent("CV"); 
		intent.putExtra("vi", viewId); 
		intent.putExtra("tid", tid); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void cvb(int viewId, String tid, int bid) {   
		Intent intent = new Intent("CVB"); 
		intent.putExtra("vi", viewId); 
		intent.putExtra("tid", tid); 
		intent.putExtra("bid", bid); 
		intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
		context.sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public String encrypt(String str) { 
		try {
	    return AesUtils.encrypt(str, AesUtils.encryptionKey); 
    } catch (Exception e) {
	    e.printStackTrace();
    	Log.e(TAG_NAME, e.getMessage(), e);
	    return null;
    }
	}

	@JavascriptInterface
	public String device() {
		return NotificationService.apkVersion + ";" + Build.MANUFACTURER + " " + Build.MODEL + ";" + Build.VERSION.SDK_INT + ";" + NotificationService.getUserUuid(context) + ";" + NotificationService.getApplicationId(context);
	}

	// Protected Methods ------------------------------------------------------------------------------ Protected Methods

	// Private Methods ---------------------------------------------------------------------------------- Private Methods

	// Getters & Setters ------------------------------------------------------------------------------ Getters & Setters

}
