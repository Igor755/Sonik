/**
 * ReceiveInstallIntent
 * 
 * receives install referer to use for our analytics to check where user installed app from
 * 
 */

package com.example.imetlin.sonik.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;



public class InstallIntent extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		//receive install referrer
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String rs = extras.getString("referrer");
			if (rs != null && rs.equals("") == false) {					
				Editor shared = sharedPreferences.edit();
				shared.putString("installedFrom", rs);
				shared.commit();
			}
		}

	}	

}
