/**
 * ReceiveRestartIntent
 * 
 * class is responsible to receive boot event and start the service to check for new routes 
 * after the device restart and notify users of new routes available
 * 
 */

package com.example.imetlin.sonik.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;



public class RestartIntent extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//lets start the service after the boot to check for new stuff
		Intent unitConverterService = new Intent(context, NotificationService.class);
		unitConverterService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(unitConverterService);
		
		//lets setup checker to check if service is running
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			NotificationServiceCheck.runChecker(context.getApplicationContext()); 

	}	

}
