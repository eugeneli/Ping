package com.pong.ping.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.pong.ping.util.SerializableBitmap;

public class Ping
{
	//JSON constants
	public static final String SERVER_ID = "ping_id";
	public static final String CREATOR_ID = "creator_id";
	public static final String CREATION_DATE = "create_date";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String HAS_IMAGE = "has_image";
	public static final String RATING = "rating";
	public static final String MESSAGE = "message";
	public static final String IMAGE = "b64image";
	
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
		json.put(SERVER_ID, serverID);
		json.put(CREATOR_ID, creatorID);
		json.put(CREATION_DATE, creationDate);
		
		json.put(LATITUDE, coordinates[0]);
		json.put(LONGITUDE, coordinates[1]);
		
		json.put(HAS_IMAGE, hasImage ? 1 : 0);
		json.put(RATING, rating);
		json.put(MESSAGE, message);
		if (hasImage)
			json.put(IMAGE, image.getBase64());
		else
			json.put(IMAGE, JSONObject.NULL);

		return json;
	}
	
	public void fromJSON(JSONObject json) throws JSONException
	{
		serverID = json.optString(SERVER_ID);
		creatorID = json.getString(CREATOR_ID);
		creationDate = json.getLong(CREATION_DATE);
		
		coordinates = new double[2];
		coordinates[0] = json.getDouble(LATITUDE);
		coordinates[1] = json.getDouble(LONGITUDE);
		
		hasImage = json.getInt(HAS_IMAGE) == 1;
		rating = json.getInt(RATING);
		message = json.optString(MESSAGE);
		String imageStr = json.optString(IMAGE);
		if (hasImage && !imageStr.equals(""))
			image = new SerializableBitmap(imageStr);
		else
			image = null;
	}
}