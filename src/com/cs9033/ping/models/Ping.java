package com.cs9033.ping.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.cs9033.ping.util.SerializableBitmap;

public class Ping
{
	//JSON constants
	public static final String JSON_SERVER_ID = "ping_id";
	public static final String JSON_CREATOR_ID = "creator_id";
	public static final String JSON_CREATION_DATE = "create_date";
	public static final String JSON_LAT = "latitude";
	public static final String JSON_LON = "longitude";
	public static final String JSON_HAS_IMAGE = "has_image";
	public static final String JSON_RATING = "rating";
	public static final String JSON_MESSAGE = "message";
	public static final String JSON_IMAGE = "b64image";
	
	//metadata
	private String serverID; //UUID on server
	private String creatorID; //UUID on server
	private long creationDate; //unix time stamp
	private double[] coordinates; //[latitude, longitude]
	private boolean hasImage;
	private int rating;
	
	//content
	private String message;
	private SerializableBitmap image;
	
	public Ping(User creator)
	{
		creatorID = creator.getUserID();
		Time currentTime = new Time();
		currentTime.setToNow();
		creationDate = currentTime.toMillis(false);
		coordinates = new double[2];
		hasImage = false;
		rating = 0;
		message = null;
		image = null;
	}
	
	public Ping(JSONObject json) throws JSONException
	{
		fromJSON(json);
	}
	
	//Setters/getters
	public void setServerID(String id) { serverID = id; }
	public String getServerID() { return serverID; }
	
	public void setCreatorID(String id) { creatorID = id; }
	public String getCreatorID() { return creatorID; }
	
	public void setCreationDate(long date) { creationDate = date; }
	public long getCreationDate() { return creationDate; }
	
	public void setCoordinates(double latitude, double longitude) { coordinates = new double[]{latitude, longitude}; }
	public double[] getCoordinates() { return coordinates; }
	
	public boolean hasImage() { return hasImage; }
	
	public int getRating() { return rating; }
	public void rateUp() { rating++; }
	public void rateDown() { rating--; }
	
	//Content methods
	public void setMessage(String msg) { message = msg; }
	public String getMessage() { return message; }
	
	public void setImage(SerializableBitmap img) { image = img; hasImage = (img != null); }
	public SerializableBitmap getImage() { return image; }
	
	//Serialization methods
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_SERVER_ID, serverID);
		json.put(JSON_CREATOR_ID, creatorID);
		json.put(JSON_CREATION_DATE, creationDate);
		
		json.put(JSON_LAT, coordinates[0]);
		json.put(JSON_LON, coordinates[1]);
		
		json.put(JSON_HAS_IMAGE, hasImage);
		json.put(JSON_RATING, rating);
		json.put(JSON_MESSAGE, message);
		if (hasImage)
			json.put(JSON_IMAGE, image.getBase64());
		else
			json.put(JSON_IMAGE, JSONObject.NULL);

		return json;
	}
	
	public void fromJSON(JSONObject json) throws JSONException
	{
		serverID = json.optString(JSON_SERVER_ID);
		creatorID = json.getString(JSON_CREATOR_ID);
		creationDate = json.getLong(JSON_CREATION_DATE);
		
		coordinates = new double[2];
		coordinates[0] = json.getDouble(JSON_LAT);
		coordinates[1] = json.getDouble(JSON_LON);
		
		hasImage = json.getBoolean(JSON_HAS_IMAGE);
		rating = json.getInt(JSON_RATING);
		message = json.optString(JSON_MESSAGE);
		String imageStr = json.optString(JSON_IMAGE);
		if (hasImage && imageStr != null)
			image = new SerializableBitmap(json.getString(JSON_IMAGE));
		else
			image = null;
	}
}
