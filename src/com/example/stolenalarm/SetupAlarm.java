package com.example.stolenalarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SetupAlarm extends Activity {

	private WakeLock wakelock;
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_alarm);
		
		setAlarm();
		
		/**
		 * Unlock Temporary  the phone
		 */
		km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("mystolenkeyguardlock");
		kl.disableKeyguard();
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | 
				PowerManager.ACQUIRE_CAUSES_WAKEUP | 
				PowerManager.ON_AFTER_RELEASE, 
				"stolenwakelock");
		
		wakelock.acquire();
	}
	
	
	private void setAlarm(){
		Calendar cal = Calendar.getInstance();
		
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(
				getApplicationContext(), 
				12345, 
				intent, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		// set to 1000*20 for faster Alarm
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*28, pending);
		
		// run thread
		runThis();
	}
	
	
	/**
	 * This will be called for closing the Activity
	 */
	private void runThis(){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(1900);
					finish();
				} catch (Exception e) {
					Log.e("[runThis]", e.getMessage());
				}
			}
		});
		t.start();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		Toast.makeText(this, "Back Button is Disabled.!", Toast.LENGTH_SHORT).show();
		Log.e("[SetupAlarm]", "onBackPressed()");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		try {
			wakelock.release();
		} catch (Exception e) {
			Log.e("[SetupAlarm]", "Wakelock release error");
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// lock the phone
		kl.reenableKeyguard();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup_alarm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
