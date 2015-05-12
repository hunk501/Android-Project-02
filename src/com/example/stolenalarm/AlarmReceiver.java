package com.example.stolenalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	private DBHelper db;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Initialize Database
		db = new DBHelper(context);
		
		/**
		 * Check the Alarm if it is Activated
		 */
		boolean isDeactivated = db.isAlarmDeactivated();
		if(!isDeactivated){
			// Alarm is Activated, start the Alarm
			Intent i = new Intent(context, AlarmActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			context.startActivity(i);
		}
	
		
	}

}
