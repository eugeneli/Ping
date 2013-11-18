package com.cs9033.ping.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.util.SerializableBitmap;

public class Ping
{
	//JSON constants
	private static final String JSON_SERVER_ID = "id";
	private static final String JSON_CREATOR_ID = "creator_id";
	private static final String JSON_CREATION_DATE = "create_date";
	private static final String JSON_LAT = "latitude";
	private static final String JSON_LON = "longitude";
	private static final String JSON_HAS_IMAGE = "has_image";
	private static final String JSON_RATING = "rating";
	private static final String JSON_MESSAGE = "message";
	private static final String JSON_IMAGE = "b64image";
	
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
		
		json.put(JSON_LAT, coordinates[0]);
		json.put(JSON_LON, coordinates[1]);
		/*JSONArray coords = new JSONArray();
		coords.put(coordinates[0]);
		coords.put(coordinates[1]);
		json.put(JSON_COORDS, coords);*/
		
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
		
		coordinates[0] = json.getDouble(JSON_LAT);
		coordinates[1] = json.getDouble(JSON_LON);
		
		hasImage = json.getBoolean(JSON_HAS_IMAGE);
		rating = json.getInt(JSON_RATING);
		message = json.getString(JSON_MESSAGE);
		image = new SerializableBitmap(json.getString(JSON_IMAGE));
	}
}
