package com.example.stolenalarm;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class AlarmActivity extends Activity implements TextToSpeech.OnInitListener {
	
	
	private WakeLock wakelock;
	private KeyguardManager km;
	private KeyguardManager.KeyguardLock kl;
	private AlarmManager alarm;
	
	private SMSReceiver smsreceiver = new SMSReceiver();
	private TextToSpeech tts;
	
	private AudioManager audio;
	
	// Alarm
	private static boolean KEEPGOING = true;
	private Thread alarm_thread;
	private Thread validate_thread;
	
	// Database
	private DBHelper db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hide status bar
		hideStatusBar();
		setContentView(R.layout.activity_alarm);
		
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		tts = new TextToSpeech(this, this);
		
		// Unlock Screen
		//unlockScreen();
		km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("StolenkeyguardLock");
		// temporarily disable the keyguard lock
		kl.disableKeyguard();
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		
		wakelock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | 
				PowerManager.ACQUIRE_CAUSES_WAKEUP | 
				PowerManager.ON_AFTER_RELEASE, 
				"stolenwakelock");
		
		wakelock.acquire();
		
		// Initialize Database
		db = new DBHelper(this);
	}
	
	/**
	 * Hide Status bar so that Notification will not be
	 * Seen, because the SMS that receive contains the password
	 */
	private void hideStatusBar(){
		// check the version
		if(Build.VERSION.SDK_INT < 16){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} 
		else {
			View view = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			view.setSystemUiVisibility(uiOptions);
			
			ActionBar bar = getActionBar();
			bar.hide();
		}
	}
	
	/**
	 * Set Maximum Volume
	 */
	private void setMaxVolume(){
		audio = (AudioManager) getSystemService(AUDIO_SERVICE);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);		
	}
	
	/**
	 * Speak
	 * @param msg
	 */
	private void speakOut(String msg){
		try {
			tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * Stop the Alarm
	 */
	private void stopAlarm(){
		
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(
				getApplicationContext(), 
				12345, 
				i, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Cancel
		alarm.cancel(pending);
		
		// disabled
		//smsreceiver.diactivateAlarm(this);
		
		Toast.makeText(this, "Alarm was Stop.!", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		Toast.makeText(this, "Back Button is Disabled.!", Toast.LENGTH_SHORT).show();
		Log.e("[AlarmActivity]", "onBackPressed()");
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			Log.e("[onKeyUp]", "Volume button was Click.!");
			setMaxVolume();
			return false;
		} 
		else if(keyCode == KeyEvent.KEYCODE_BACK){
			Log.e("[onKeyUp]", "Back button was Click.!");
			return false;
		}
		return false;
		//return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.e("[AlarmActivity]", "onStart()");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("[AlarmActivity]", "onResume()");
		/**
		 * Start the Speaking
		 * Alarm
		 */
		if(alarm_thread == null){
			alarm_thread = new Thread(new SpeakThread());
			alarm_thread.start();
		}
		
		/**
		 * Start the Validate Alarm
		 */
		if(validate_thread == null){
			validate_thread = new Thread(new Validate());
			validate_thread.start();
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e("[AlarmActivity]", "onPause()");
		try {
			wakelock.release();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e("[AlarmActivity]", "onDestroy()");
		try {
			tts.stop();
			tts.shutdown();
			kl.reenableKeyguard();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}


	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
			tts.setLanguage(Locale.US);
		}
	}
	
	
	/**
	 * This thread will be triggered for Speaking
	 * Alarm
	 * @author Dennis
	 *
	 */
	class SpeakThread implements Runnable{
		private int lop = 0;
		@Override
		public void run() {
			while(KEEPGOING){
				try {
					// set Volume to Maximum
					setMaxVolume();
					// Speak Alarm
					speakOut("please help! my cellphone! was stolen!");
					// Logged
					lop++;
					Log.e("[SpeakThread]", "Loop: "+ lop);
					// Sleep for a while
					Thread.sleep(2400);
					
				} catch (Exception e) {
					Log.e("[SpeakThread]", e.getMessage());
				}
			}
			
			if(!KEEPGOING){
				//Toast.makeText(getApplicationContext(), "Stolen Alarm has been Deactivated.!", Toast.LENGTH_LONG).show();
				finish();				
			}
		}
		
	}
	
	/**
	 * This Thread will check on the database for deactivate
	 * of the Alarm
	 * @author Dennis
	 *
	 */
	class Validate implements Runnable{

		@Override
		public void run() {
			while(KEEPGOING){
				try {
					// Check the database
					boolean isDeactivate = db.isAlarmDeactivated();
					if(isDeactivate){
						KEEPGOING = false;
					}
					// Sleep for a while
					Thread.sleep(17000);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
}
