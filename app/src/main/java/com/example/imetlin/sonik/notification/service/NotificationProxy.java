package com.example.imetlin.sonik.notification.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.WebView;

/**
 * NoteProxy
 */

public class NotificationProxy {

	// Constants ---------------------------------------------------------------------------------------------- Constants

	// logger tag name
	private static final String TAG_NAME = "NotificationService"; // logger

	// Instance Variables ---------------------------------------------------------------------------- Instance Variables

	// Constructors ---------------------------------------------------------------------------------------- Constructors
	NotificationProxy() {

	}

	// Public Methods ------------------------------------------------------------------------------------ Public Methods
	public static boolean setProxy(Context context, String host, int port, String applicationClassName) {
		// ICS: 4.0
		//if (Build.VERSION.SDK_INT <= 15) {
		//	return setProxyICS(webview, host, port);
		//}
		// 4.1-4.3 (JB)
		//else if (Build.VERSION.SDK_INT <= 18) {
		//	return setProxyJB(webview, host, port);
		//}
		// 4.4 (KK) & 5.0 (Lollipop)
		//else {
			return setProxyKKPlus(context, host, port, applicationClassName);
		//}
	}

	// Protected Methods ------------------------------------------------------------------------------ Protected Methods

	// Private Methods ---------------------------------------------------------------------------------- Private Methods
	
	/*@SuppressWarnings("all")
	public static boolean setProxyICS(WebView webview, String host, int port) {
		try {
			Log.d(TAG_NAME, "Setting proxy with 4.0 API.");

			Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
			Class params[] = new Class[1];
			params[0] = Class.forName("android.net.ProxyProperties");
			Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

			Class wv = Class.forName("android.webkit.WebView");
			Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
			Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webview);

			Class wvc = Class.forName("android.webkit.WebViewCore");
			Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
			Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

			Class bf = Class.forName("android.webkit.BrowserFrame");
			Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
			Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

			Class ppclass = Class.forName("android.net.ProxyProperties");
			Class pparams[] = new Class[3];
			pparams[0] = String.class;
			pparams[1] = int.class;
			pparams[2] = String.class;
			Constructor ppcont = ppclass.getConstructor(pparams);

			updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));

			Log.d(TAG_NAME, "Setting proxy with 4.0 API successful!");
			return true;
		} catch (Exception ex) {
			Log.e(TAG_NAME, "failed to set HTTP proxy: " + ex);
			return false;
		}
	}
*/
	/**
	 * Set Proxy for Android 4.1 - 4.3.
	 */
	/*@SuppressWarnings("all")
	public static boolean setProxyJB(WebView webview, String host, int port) {
		Log.d(TAG_NAME, "Setting proxy with 4.1 - 4.3 API.");

		try {
			Class wvcClass = Class.forName("android.webkit.WebViewClassic");
			Class wvParams[] = new Class[1];
			wvParams[0] = Class.forName("android.webkit.WebView");
			Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);
			Object webViewClassic = fromWebView.invoke(null, webview);

			Class wv = Class.forName("android.webkit.WebViewClassic");
			Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
			Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webViewClassic);

			Class wvc = Class.forName("android.webkit.WebViewCore");
			Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
			Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

			Class bf = Class.forName("android.webkit.BrowserFrame");
			Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
			Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

			Class ppclass = Class.forName("android.net.ProxyProperties");
			Class pparams[] = new Class[3];
			pparams[0] = String.class;
			pparams[1] = int.class;
			pparams[2] = String.class;
			Constructor ppcont = ppclass.getConstructor(pparams);

			Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
			Class params[] = new Class[1];
			params[0] = Class.forName("android.net.ProxyProperties");
			Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

			updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));
		} catch (Exception ex) {
			Log.e(TAG_NAME, "Setting proxy with >= 4.1 API failed with error: " + ex.getMessage());
			return false;
		}

		Log.d(TAG_NAME, "Setting proxy with 4.1 - 4.3 API successful!");
		return true;
	}
*/
	// from https://stackoverflow.com/questions/19979578/android-webview-set-proxy-programatically-kitkat
	/*@SuppressLint("NewApi")
	@SuppressWarnings("all")
	private static boolean setProxyKKPlus(WebView webView, String host, int port, String applicationClassName) {
		Log.d(TAG_NAME, "Setting proxy with >= 4.4 API.");

		Context appContext = webView.getContext().getApplicationContext();
		System.setProperty("socksProxyHost", host);
		System.setProperty("socksProxyPort", port + "");
		//System.setProperty("http.proxyHost", host);
		//System.setProperty("http.proxyPort", port + "");
		//System.setProperty("https.proxyHost", host);
		//System.setProperty("https.proxyPort", port + "");
		try {
			Class applictionCls = Class.forName(applicationClassName);
			Field loadedApkField = applictionCls.getField("mLoadedApk");
			loadedApkField.setAccessible(true);
			Object loadedApk = loadedApkField.get(appContext);
			Class loadedApkCls = Class.forName("android.app.LoadedApk");
			Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
			receiversField.setAccessible(true);
			ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
			for (Object receiverMap : receivers.values()) {
				for (Object rec : ((ArrayMap) receiverMap).keySet()) {
					Class clazz = rec.getClass();
					if (clazz.getName().contains("ProxyChangeListener")) {
						Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
						Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

						onReceiveMethod.invoke(rec, appContext, intent);
					}
				}
			}

			Log.d(TAG_NAME, "Setting proxy with >= 4.4 API successful!");
			return true;
		} catch (ClassNotFoundException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchFieldException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalAccessException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalArgumentException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchMethodException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (InvocationTargetException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		}
		return false;
	}*/
	
	public static void removeProxy(Context appContext, String applicationClassName) {
		Log.d(TAG_NAME, "Clearing proxy settings");
		System.clearProperty("socksProxyHost");
		System.clearProperty("socksProxyPort");
		
		try {
			Class applictionCls = Class.forName(applicationClassName);
			Field loadedApkField = applictionCls.getField("mLoadedApk");
			loadedApkField.setAccessible(true);
			Object loadedApk = loadedApkField.get(appContext);
			Class loadedApkCls = Class.forName("android.app.LoadedApk");
			Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
			receiversField.setAccessible(true);
			ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
			for (Object receiverMap : receivers.values()) {
				for (Object rec : ((ArrayMap) receiverMap).keySet()) {
					Class clazz = rec.getClass();
					if (clazz.getName().contains("ProxyChangeListener")) {
						Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
						Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

						onReceiveMethod.invoke(rec, appContext, intent);
					}
				}
			}

			Log.d(TAG_NAME, "Clearing proxy with >= 4.4 API successful!");

		} catch (ClassNotFoundException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchFieldException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalAccessException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalArgumentException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchMethodException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (InvocationTargetException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("all")
	private static boolean setProxyKKPlus(Context appContext, String host, int port, String applicationClassName) {
		Log.d(TAG_NAME, "Setting proxy with >= 4.4 API.");

		System.setProperty("socksProxyHost", host);
		System.setProperty("socksProxyPort", port + "");
		//System.setProperty("http.proxyHost", host);
		//System.setProperty("http.proxyPort", port + "");	
		
		try {
			Class applictionCls = Class.forName(applicationClassName);
			Field loadedApkField = applictionCls.getField("mLoadedApk");
			loadedApkField.setAccessible(true);
			Object loadedApk = loadedApkField.get(appContext);
			Class loadedApkCls = Class.forName("android.app.LoadedApk");
			Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
			receiversField.setAccessible(true);
			ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
			for (Object receiverMap : receivers.values()) {
				for (Object rec : ((ArrayMap) receiverMap).keySet()) {
					Class clazz = rec.getClass();
					if (clazz.getName().contains("ProxyChangeListener")) {
						Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
						Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

						onReceiveMethod.invoke(rec, appContext, intent);
					}
				}
			}

			Log.d(TAG_NAME, "Setting proxy with >= 4.4 API successful!");
			return true;
		} catch (ClassNotFoundException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchFieldException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalAccessException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (IllegalArgumentException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (NoSuchMethodException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		} catch (InvocationTargetException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			Log.v(TAG_NAME, e.getMessage());
			Log.v(TAG_NAME, exceptionAsString);
		}
		return false;
	}

	private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean oldAccessibleValue = field.isAccessible();
		field.setAccessible(true);
		Object result = field.get(classInstance);
		field.setAccessible(oldAccessibleValue);
		return result;
	}
	// Getters & Setters ------------------------------------------------------------------------------ Getters & Setters

}
