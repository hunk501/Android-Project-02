package com.example.stolenalarm;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private EditText ac_pass, dc_pass;
	private Button btnSave;
	
	private DBHelper db;
	
	private SMSReceiver sms = new SMSReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ac_pass = (EditText) findViewById(R.id.editText1);
		dc_pass = (EditText) findViewById(R.id.editText2);
		
		btnSave = (Button) findViewById(R.id.button1);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ContentValues values = new ContentValues();
				values.put("_ac_password", ac_pass.getText().toString());
				values.put("_dc_password", dc_pass.getText().toString());
				values.put("_keepgoing", "0");
				
				db.insert("tbl_setting", values);
				
				Cursor cursor = db.displayRecords("tbl_setting");
				if(cursor.moveToFirst()){
					do{
						String id = cursor.getString(0);
						String acpasw = cursor.getString(1);
						String dcpasw = cursor.getString(2);
						String keep_going = cursor.getString(3);
						
						Log.e("[Records]", "Id="+ id +", AC="+ acpasw +", DC="+ dcpasw +", Activate="+ keep_going);
					} while(cursor.moveToNext());
				}
				
				Toast.makeText(MainActivity.this, "Password has been saved.!", Toast.LENGTH_LONG).show();
				
				// reload 
				reloadThis();
			}
		});
		
		
		// Initialize Database
		db = new DBHelper(this);
		// Check the database if has an records
		Cursor cursor = db.displayRecords("tbl_setting");
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					
					String ac = cursor.getString(1);
					String dc = cursor.getString(2);
					
					ac_pass.setText(ac);
					dc_pass.setText(dc);
					
				} while(cursor.moveToNext());
			} else {
				Log.e("[MainActivity]", "No records found");
			}
		}
	}
	
	/**
	 * Reload the activity
	 */
	private void reloadThis(){
		finish();
		startActivity(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
