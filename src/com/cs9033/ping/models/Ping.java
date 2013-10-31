package com.cs9033.ping.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.util.SerializableBitmap;

public class Ping
{
	//JSON constants
	private static final String JSON_SERVER_ID = "ping_serverid";
	private static final String JSON_CREATOR_ID = "trip_creatorid";
	private static final String JSON_CREATION_DATE = "ping_creationdate";
	private static final String JSON_COORDS = "ping_coords";
	private static final String JSON_HAS_IMAGE = "ping_hasimage";
	private static final String JSON_RATING = "ping_rating";
	private static final String JSON_MESSAGE = "ping_message";
	private static final String JSON_IMAGE = "ping_image";
	
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
	
	public Ping() {} //Write other constructors as needed later on
	
	//Setters/getters
	public void setServerID(String id) { serverID = id; }
	public String getServerID() { return serverID; }
	
	public void setCreatorID(String id) { creatorID = id; }
	public String getCreatorID() { return creatorID; }
	
	public void setCreationDate(long date) { creationDate = date; }
	public long getCreationDate() { return creationDate; }
	
	public void setCoordinates(long latitude, long longitude) { coordinates = new double[]{latitude, longitude}; }
	public double[] getCoordinates() { return coordinates; }
	
	public boolean hasImage() { return hasImage; }
	
	public int getRating() { return rating; }
	public void rateUp() { rating++; }
	public void rateDown() { rating--; }
	
	//Content methods
	public void setMessage(String msg) { message = msg; }
	public String getMessage() { return message; }
	
	public void setImage(SerializableBitmap img) { image = img; }
	public SerializableBitmap getImage() { return image; }
	
	//Serialization methods
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_SERVER_ID, serverID);
		json.put(JSON_CREATOR_ID, creatorID);
		json.put(JSON_CREATION_DATE, creationDate);
		
		JSONArray coords = new JSONArray();
		coords.put(coordinates[0]);
		coords.put(coordinates[1]);
		json.put(JSON_COORDS, coords);
		
		json.put(JSON_HAS_IMAGE, hasImage);
		json.put(JSON_RATING, rating);
		json.put(JSON_MESSAGE, message);
		json.put(JSON_IMAGE, image.getBase64());

		return json;
	}
	
	public void fromJSON(JSONObject json) throws JSONException
	{
		serverID = json.getString(JSON_SERVER_ID);
		creatorID = json.getString(JSON_CREATOR_ID);
		creationDate = json.getLong(JSON_CREATION_DATE);
		
		coordinates[0] = json.getJSONArray(JSON_COORDS).getDouble(0);
		coordinates[1] = json.getJSONArray(JSON_COORDS).getDouble(1);
		
		hasImage = json.getBoolean(JSON_HAS_IMAGE);
		rating = json.getInt(JSON_RATING);
		message = json.getString(JSON_MESSAGE);
		image = new SerializableBitmap(json.getString(JSON_IMAGE));
	}
}
