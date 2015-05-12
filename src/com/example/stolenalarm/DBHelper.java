package com.example.stolenalarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelper {
	
	private Context context;
	private SQL sql;
	
	public DBHelper(Context con){
		this.context = con;
		// initialize SQLiteOpenHelper
		sql = new SQL(context);
	}
	
	/**
	 * Open the database and then return it.
	 * @return
	 */
	private synchronized SQLiteDatabase connect(){
		return sql.getWritableDatabase();
	}
	
	
	/**
	 * ----------------- CRUD --------------------------
	 */
	
	public void insert(String table, ContentValues values){
		
		int count = getCount(table);
		
		// Check the total records if > 0 Update else insert
		final SQLiteDatabase db = connect();
		if(count > 0){
			try {
				db.update(table, values, "_id=?", new String[]{"1"});
				Log.e("[update]", "records has been updated.");
			} catch (Exception e) {
				displayMsg("[Insert]: "+e.getMessage());
			} finally{
				db.close();
			}
		} 
		else {
			try {
				db.insert(table, null, values);
				Log.e("[insert]", "records has been inserted.");
			} catch (Exception e) {
				displayMsg("[Insert]: "+e.getMessage());
			} finally{
				db.close();
			}
		}
	}
	
	public Cursor displayRecords(String table){
		final SQLiteDatabase db = connect();
		try {
			Cursor cursor = db.rawQuery("Select * from "+ table, null);
			if(cursor.moveToFirst()){
				return cursor;
			}
		} catch (Exception e) {
			displayMsg("[displayRecords]: "+e.getMessage());
		}
		
		return null;
	}
	
	public int getCount(String table){
		final SQLiteDatabase db = connect();
		Cursor  c = db.rawQuery("Select * from "+ table, null);
		
		if(c.moveToFirst()){
			return c.getCount();
		}
		
		return 0;
	}
	
	public Password getPassword(){
		final SQLiteDatabase db = connect();
		Cursor c = db.rawQuery("Select * from tbl_setting", null);
		Password pas = new Password();
		
		if(c.moveToFirst()){
			pas.setActivatePassword(c.getString(1));
			pas.setDiactivatePassword(c.getString(2));
		}
		
		c.close();
		return pas;
	}
	
	public void update(String table, ContentValues values, String whereClause, String[] whereArgs){
		final SQLiteDatabase db = connect();
		try {
			db.update(table, values, whereClause, whereArgs);
		} catch (Exception e) {
			//displayMsg("[Insert]: "+e.getMessage());
		} finally{
			db.close();
		}
	}
	
	public boolean isAlarmDeactivated(){
		boolean result = false;
		final SQLiteDatabase db = connect();
		try {
			Cursor c = db.rawQuery("Select _keepgoing from tbl_setting where _id=1", null);
			if(c.moveToFirst()){
				int data = Integer.parseInt(c.getString(0));
				result = (data == 0) ? true : false;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally{
			db.close();
		}
		return result;
	}
	
	/**
	 * ----------------- /CRUD --------------------------
	 */
	
	/**
	 * Display Message
	 * @param msg
	 */
	private void displayMsg(String msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	
	/**
	 * SQLite Open Helper
	 * @author Dennis
	 *
	 */
	private class SQL extends SQLiteOpenHelper{
		
		private static final String DB_NAME = "mydb.db";
		private static final int DB_VERSION = 1;
		
		private static final String QUERY1 = "CREATE TABLE tbl_setting("
				+ "_id integer primary key autoincrement,"
				+ "_ac_password text,"
				+ "_dc_password text,"
				+ "_keepgoing integer);";
		private static final String QUERY2 = "CREATE TABLE tbl_block("
				+ "_id integer primary key autoincrement,"
				+ "_contact text);";	
		
		public SQL(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(QUERY1);
			db.execSQL(QUERY2);
			Log.e("[SQL]", "Database has been created.!");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
			
		}
	}
}
