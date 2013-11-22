package com.cs9033.ping.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User
{
	private String name; //Display name
	private String userID; //UUID on server
	private double radius; //Radius to receive pings
	private int numPingsRemaining; //Limit a user's number of pings per day
	private String authToken; //Authentication token to access API
	
	//JSON Constants
	public static final String JSON_USER_NAME = "name";
	public static final String JSON_USER_PWD = "password";
	public static final String JSON_USER_ID = "user_id";
	public static final String JSON_USER_RADIUS = "radius";
	public static final String JSON_USER_REMAINING_PINGS = "remaining_pings";
	public static final String JSON_AUTH_TOKEN = "auth";
	
	public User() {}
	
	public User(String id)
	{
		//call method to retrieve user info from server based on id? 
	}
	
	public User(JSONObject json) throws JSONException { fromJSON(json); }
	
	//Setters/getters
	public void setName(String aName) { name = aName; }
	public String getName() { return name; }
	
	public String getUserID() { return userID; } //No setter for UserID. Don't see why we need it.
	
	public void setRadius(double rad) { radius = rad; }
	public double getRadius() { return radius; }
	
	public void setPingLimit(int num) { numPingsRemaining = num; }
	public int getRemainingPings() { return numPingsRemaining; }
	
	public void setAuthToken(String token) { authToken = token; }
	public String getAuthToken() { return authToken; }
	
	//Serialization methods
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_USER_NAME, name);
		json.put(JSON_USER_ID, userID);
		json.put(JSON_USER_RADIUS, radius);
		json.put(JSON_USER_REMAINING_PINGS, numPingsRemaining);
		json.put(JSON_AUTH_TOKEN, authToken);
		return json;
	}
	
	public void fromJSON(JSONObject json) throws JSONException
	{
		name = json.getString(JSON_USER_NAME);
		userID = json.getString(JSON_USER_ID);
		radius = json.getDouble(JSON_USER_RADIUS);
		numPingsRemaining = json.getInt(JSON_USER_REMAINING_PINGS);
		authToken = json.getString(JSON_AUTH_TOKEN);
	}
}
