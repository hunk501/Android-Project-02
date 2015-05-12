package com.example.stolenalarm;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends WakefulBroadcastReceiver{

	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private String from;
	private String msg_body;
	
	// Database helper
	private DBHelper db;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		/**
		 * initialize database
		 */
		db = new DBHelper(context);
		
		/**
		 * Check the Intent Action
		 */
		if(intent.getAction().equals(ACTION)){
			
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] message = new SmsMessage[pdus.length];
				
				for(int i=0; i < pdus.length; i++){
					message[i] = SmsMessage.createFromPdu( (byte[]) pdus[i] );
				}
				
				/**
				 * Loop through message
				 */
				for(SmsMessage msg : message){
					from = msg.getOriginatingAddress();
					msg_body = msg.getDisplayMessageBody();
				}
				
				/**
				 * Check if the Contact Number has more than 2 records 
				 * in the database
				 */
				int countRows = db.getCount("tbl_block");
				if(countRows <= 2){ // Contact is not block
					/**
					 * Get the Save password
					 */
					Password pas = db.getPassword();
					
					/**
					 * check the message Actions
					 * all message will be split by ':' symbol
					 * 
					 * example: [action]:[password]
					 */
					
					try {
						String[] str = msg_body.split(":");
						String actions = str[0]; // get the Actions, set to lower case
						String password = str[1]; 			   // Get the Action password
						switch(actions.toLowerCase()){
						
							case "ac":
								// now check the password if matched
								if(password.equals(pas.getActivatePassword())){									
									/**
									 * Activate Alarm
									 */
									ContentValues values = new ContentValues();
									values.put("_keepgoing", "1");
									// update Database
									db.update("tbl_setting", values, "_id=?", new String[]{"1"});
									
									/**
									 * Start Alarm Activity
									 */
									Intent alarm_intent = new Intent(context, AlarmActivity.class);
									// set Activity to new task to bring it to the front
									alarm_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									// start Intent
									context.startActivity(alarm_intent);
									
									/**
									 * Enable BootLoader
									 */
									activateBootLoader(context);
								} 
								else {
									/**
									 * Key was valid but the password was incorrect so let's save this
									 * for counting
									 */									
									insetData(from);
								}
								break;
								
								
							case "dc":
								// Now check the password if matched
								if(password.equals(pas.getDiactivatePassword())){									
									/**
									 * Deactivate Alarm and BootLoader
									 */
									ContentValues values = new ContentValues();
									values.put("_keepgoing", "0");
									
									db.update("tbl_setting", values, "_id=?", new String[]{"1"});
									
									deactivateBootLoader(context);
									
								} else {
									/**
									 * Key was valid but the password was incorrect so let's save this
									 * for counting
									 */									
									insetData(from);
								}
								break;
								
								
							default:
								//Toast.makeText(context, "action: "+str[0], Toast.LENGTH_LONG).show();
								//Log.e("[SMSReceiver]", "Unkown Action");
								break;
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
				else {
					//Toast.makeText(context, "Contact Number is in the Blocked lists.!", Toast.LENGTH_LONG).show();
				}
				
				
			}
		}
		
	}
	
	
	/**
	 * This will insert the Data to Database
	 * for Incorrect Password for Counting
	 * @param frm  - This is the Contact Number
	 */
	private void insetData(String frm){
		try {
			ContentValues values = new ContentValues();
			values.put("_contact", frm);
			
			db.insert("tbl_block", values);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	/**
	 * This will Activate the Alarm
	 */
	public void activateBootLoader(Context context){
		ComponentName comp = new ComponentName(context, AlarmReceiver.class);
		PackageManager pack = context.getPackageManager();
		
		pack.setComponentEnabledSetting(
				comp, 
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
				PackageManager.DONT_KILL_APP);
		
		Log.e("[Activate]", "AlarmReceiver was Activated.!");
	}
	
	
	/**
	 * This will deactivate the Alarm
	 */
	public void deactivateBootLoader(Context context){
		ComponentName comp = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();
		
		pm.setComponentEnabledSetting(
				comp, 
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
				PackageManager.DONT_KILL_APP);
		
		Log.e("[Di-Activate]", "AlarmReceiver was di-Activated.!");
	}
	
	

}
