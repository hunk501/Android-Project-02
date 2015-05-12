package com.example.stolenalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CancelAlarm extends Activity {
	
	// keyguard Lock
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	// PowerManager
	private WakeLock wakelock;
	// Alarm Manager
	private AlarmManager am;
	// SMS Receiver
	private SMSReceiver smsrecevier = new SMSReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel_alarm);
		
		// initialize Alarm Manager
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		/**
		 * Unlock temporary the Screen
		 */
		km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("mystolenkeyguardlock");
		kl.disableKeyguard();
		/**
		 * Set Phone to wake up
		 */
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | 
				PowerManager.ACQUIRE_CAUSES_WAKEUP | 
				PowerManager.ON_AFTER_RELEASE, 
				"stolenwakelock");
		
		wakelock.acquire();
	}
	
	
	/**
	 * Cancel The Alarm
	 */
	private void cancelAlarm(){
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(
				getApplicationContext(), 
				12345, 
				intent, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		am.cancel(pending);
		Log.e("[CancelAlarm]", "Alarm was Canceled.!");
		
		// disable broadcast receiver
		//smsrecevier.diactivateAlarm(this);
	}
	
	/**
	 * This will close automatically
	 * this Activity
	 */
	private void runThis(){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3500);
					finish();
				} catch (Exception e) {
					Log.e("[CancelAlarm]", "runThis():+ "+ e.getMessage());
				}
			}
		});
		
		t.start();
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		cancelAlarm();
		
		runThis();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		Toast.makeText(this, "Back Button is Disabled.!", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			wakelock.release();
		} catch (Exception e) {
			Log.e("[CancelAlarm]", "Wakelock release error");
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			//reenable the security lock
			kl.reenableKeyguard();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.cancel_alarm, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
