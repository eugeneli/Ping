package com.pong.ping.util;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.pong.ping.models.Ping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PingDatabase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Ping";
    private static final String PINGS_TABLE_NAME = "Pings";
    
	public PingDatabase(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//Create table
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_TRIPS_TABLE = "CREATE TABLE " + PINGS_TABLE_NAME + "("
                + Ping.SERVER_ID + " TEXT PRIMARY KEY,"
                + Ping.CREATOR_ID + " TEXT,"
				+ Ping.CREATION_DATE + " TEXT,"
				+ Ping.LATITUDE + " DOUBLE,"
				+ Ping.LONGITUDE + " DOUBLE,"
				+ Ping.HAS_IMAGE + " INTEGER,"
				+ Ping.RATING + " INTEGER,"
				+ Ping.MESSAGE + " TEXT,"
				+ Ping.IMAGE + " TEXT"
				+ ")";

        db.execSQL(CREATE_TRIPS_TABLE);
	}
	
	public void addPing(Ping ping)
	{
		String pingId = ping.getServerID();
		SQLiteDatabase db = this.getWritableDatabase();
		 
        ContentValues pingValues = new ContentValues();
        pingValues.put(Ping.SERVER_ID, pingId);
        pingValues.put(Ping.CREATOR_ID, ping.getCreatorID());
        pingValues.put(Ping.CREATION_DATE, ping.getCreationDate());
        pingValues.put(Ping.LATITUDE, ping.getCoordinates()[0]);
        pingValues.put(Ping.LONGITUDE, ping.getCoordinates()[1]);
        pingValues.put(Ping.HAS_IMAGE, ping.hasImage() ? "1" : "0");
        pingValues.put(Ping.RATING, ping.getRating());
        pingValues.put(Ping.MESSAGE, ping.getMessage());
        if(ping.hasImage())
        	pingValues.put(Ping.IMAGE, ping.getImage().getBase64());
        
        db.insert(PINGS_TABLE_NAME, null, pingValues);
        db.close();
	}
	
	public ArrayList<Ping> getAllPings()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Ping> pings = new ArrayList<Ping>();

        String selectPings = "SELECT * FROM " + PINGS_TABLE_NAME + " ORDER BY " + Ping.CREATION_DATE + " DESC";
        Cursor cursor = db.rawQuery(selectPings, null);
 
        if (cursor.moveToFirst())
        {
            do
            {
            	JSONObject json = new JSONObject();
        		try {
					json.put(Ping.SERVER_ID, cursor.getString(0));
					json.put(Ping.CREATOR_ID, cursor.getString(1));
	        		json.put(Ping.CREATION_DATE, cursor.getLong(2));
	        		
	        		json.put(Ping.LATITUDE, cursor.getDouble(3));
	        		json.put(Ping.LONGITUDE, cursor.getDouble(4));
	        		
	        		json.put(Ping.HAS_IMAGE, (cursor.getInt(5) == 1) ? 1 : 0);
	        		json.put(Ping.RATING, cursor.getInt(6));
	        		json.put(Ping.MESSAGE, cursor.getString(7));
	        		if (cursor.getInt(5) == 1)
	        			json.put(Ping.IMAGE, cursor.getString(8));
	        		else
	        			json.put(Ping.IMAGE, JSONObject.NULL);
	        		
	        		Ping aPing = new Ping(json);
	               
	               //Add ping to ArrayList
	               pings.add(aPing);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            while (cursor.moveToNext());
        }
        cursor.close();
 
        db.close();

        //return pings
        return pings;
	}
	
	/*public Ping getPing(String id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Ping> pings = new ArrayList<Ping>();

        String selectPings = "SELECT * FROM " + PINGS_TABLE_NAME + "WHERE  ORDER BY " + Ping.CREATION_DATE + " DESC";
        Cursor cursor = db.rawQuery(selectPings, null);
 
        if (cursor.moveToFirst())
        {
            do
            {
            	JSONObject json = new JSONObject();
        		try {
					json.put(Ping.SERVER_ID, cursor.getString(0));
					json.put(Ping.CREATOR_ID, cursor.getString(1));
	        		json.put(Ping.CREATION_DATE, cursor.getLong(2));
	        		
	        		json.put(Ping.LATITUDE, cursor.getDouble(3));
	        		json.put(Ping.LONGITUDE, cursor.getDouble(4));
	        		
	        		json.put(Ping.HAS_IMAGE, (cursor.getInt(5) == 1) ? 1 : 0);
	        		json.put(Ping.RATING, cursor.getInt(6));
	        		json.put(Ping.MESSAGE, cursor.getString(7));
	        		if (cursor.getInt(5) == 1)
	        			json.put(Ping.IMAGE, cursor.getString(8));
	        		else
	        			json.put(Ping.IMAGE, JSONObject.NULL);
	        		
	        		Ping aPing = new Ping(json);
	               
	               //Add ping to ArrayList
	               pings.add(aPing);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            while (cursor.moveToNext());
        }
        cursor.close();
 
        db.close();

        //return pings
        return pings;
	}*/

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		
	}
}
