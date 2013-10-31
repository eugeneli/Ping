package com.cs9033.ping.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User
{
	private String name; //Display name
	private String userID; //UUID on server
	private double radius; //Radius to receive pings
	private int numPingsRemaining; //Limit a user's number of pings per day
	
	private static final String JSON_USER_NAME = "user_name";
	private static final String JSON_USER_ID = "user_id";
	private static final String JSON_USER_RADIUS = "user_radius";
	private static final String JSON_USER_REMAINING_PINGS = "user_remaining_pings";
	
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
	
	//Serialization methods
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_USER_NAME, name);
		json.put(JSON_USER_ID, userID);
		json.put(JSON_USER_RADIUS, radius);
		json.put(JSON_USER_REMAINING_PINGS, numPingsRemaining);
		return json;
	}
	
	public void fromJSON(JSONObject json) throws JSONException
	{
		name = json.getString(JSON_USER_NAME);
		userID = json.getString(JSON_USER_ID);
		radius = json.getDouble(JSON_USER_RADIUS);
		numPingsRemaining = json.getInt(JSON_USER_REMAINING_PINGS);
	}
}
