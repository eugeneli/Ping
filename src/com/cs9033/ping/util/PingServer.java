package com.cs9033.ping.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.Ping;
import com.cs9033.ping.models.User;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PingServer
{
	private final static String TAG = "Server";
	private static String SERVER_URL;
	
	private final static String JSON_COMMAND = "command";
	private final static String JSON_DATA = "json_data";
	
	public final static String ASYNC_RESULT = "result";
	public final static String ASYNC_RESPONSE_CODE = "response_code";
	
	private static Handler completionHandler;
	
	public PingServer(String url, Handler comHandler)
	{
		SERVER_URL = url;
		completionHandler = comHandler;
	}
	
	public PingServer(String url) { SERVER_URL = url; }
	
	public void startCreateUserTask(String username, String password)
	{
		new CreateUserTask().execute(username, password);
	}
	
	public void startCreatePingTask(Object user, Object ping)
	{
		new CreatePingTask().execute(user, ping);
	}
	
	
	public void startUpdateLocationTask(double latitude, double longitude)
	{
		new UpdateLocationTask().execute(latitude, longitude);
	}
	
	/*
	 * POST: ["command"]=> "CREATE_USER"
			  ["json_data"]=> "{"password":"FDFSDFS","name":"DFDFDFDFDF"}"
	   RECEIVE: {"response_code":1,"user_id":"528ef3c977d98","auth":"59eda95dc51cb4207728e8e8d681c104"}
	 */
	private static class CreateUserTask extends AsyncTask<String, Void, String>
	{
		private final String TASK_TAG = "CreateUser";
		private String responseString = "";
		
		private final String JSON_CREATE_USER_COMMAND = "CREATE_USER";
		
        @Override
        protected String doInBackground(String... params)
        {
        	// Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(SERVER_URL);
            
            //POST data. Add the command first.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair(JSON_COMMAND, JSON_CREATE_USER_COMMAND));
            
            //Now build JSON object to be serialized and sent as 2nd POST parameter.
            JSONObject createUserJSON = new JSONObject();
            
			try
			{
				//Build json object
				createUserJSON.put(User.JSON_USER_NAME, params[0]);
				createUserJSON.put(User.JSON_USER_PWD, params[1]);
					
				//Add to POST data
				nameValuePairs.add(new BasicNameValuePair(JSON_DATA, createUserJSON.toString()));
				
				//POST to server
	            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            HttpResponse response = httpClient.execute(httpPost);
	            
	            InputStream is = response.getEntity().getContent();
	            
	            responseString = convertStreamToString(is);

	            return responseString;
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
			return responseString;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
        	Log.i(TAG+": "+TASK_TAG, result);
        	
        	if(completionHandler != null)
        	{
        		Message msg = new Message();
            	Bundle bundle = new Bundle();
            	bundle.putString(ASYNC_RESULT, result);
            	msg.setData(bundle);
            	completionHandler.dispatchMessage(msg);
        	}
        }
    }
	
	private static class CreatePingTask extends AsyncTask<Object, Void, String>
	{
		private final String TASK_TAG = "CreatePing";
		private String responseString = "";
		
		private final String JSON_CREATE_PING_COMMAND = "TRIP_STATUS";
		private final String JSON_PING_DATA = "ping_data";

        @Override
        protected String doInBackground(Object... params)
        {
        	//lol java
        	User user = (User)params[0];
        	Ping ping = (Ping)params[1];
        	
        	// Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(SERVER_URL);
            
            //POST data. Add the command first.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair(JSON_COMMAND, JSON_CREATE_PING_COMMAND));
            
            //Now build JSON object to be serialized and sent as 2nd POST parameter.
            JSONObject createPingJSON = new JSONObject();
            
			try
			{
				//Build json object
				createPingJSON.put(User.JSON_USER_ID, user.getUserID());
				createPingJSON.put(User.JSON_AUTH_TOKEN, user.getAuthToken());
				
				createPingJSON.put(JSON_PING_DATA,  ping.toJSON().toString());
				
				/*createPingJSON.put(Ping.JSON_CREATOR_ID, ping.getCreatorID());
				createPingJSON.put(Ping.JSON_CREATION_DATE, ping.getCreationDate());
				createPingJSON.put(Ping.JSON_LAT, ping.getCoordinates()[0]);
				createPingJSON.put(Ping.JSON_LON, ping.getCoordinates()[1]);
				createPingJSON.put(Ping.JSON_HAS_IMAGE, ping.hasImage());
				createPingJSON.put(Ping.JSON_RATING, ping.getRating());
				createPingJSON.put(Ping.JSON_MESSAGE, ping.getMessage());
				createPingJSON.put(Ping.JSON_IMAGE, ping.getImage().getBase64());*/
					
				//Add to POST data
				nameValuePairs.add(new BasicNameValuePair(JSON_DATA, createPingJSON.toString()));
				
				//POST to server
	            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            HttpResponse response = httpClient.execute(httpPost);
	            
	            InputStream is = response.getEntity().getContent();
	            
	            responseString = convertStreamToString(is);

	            return responseString;
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
			return responseString;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
        	Log.i(TAG+": "+TASK_TAG, result);
        	
        	if(completionHandler != null)
        	{
        		Message msg = new Message();
            	Bundle bundle = new Bundle();
            	bundle.putString(ASYNC_RESULT, result);
            	msg.setData(bundle);
            	completionHandler.dispatchMessage(msg);
        	}
        }
    }
	
	//Successful Response: {"response_code": 0}
	private static class UpdateLocationTask extends AsyncTask<Double, Void, String>
	{
		private final String TASK_TAG = "UpdateLoc";
		private String result = "";
		private final String JSON_UPDATE_LOCATION_COMMAND = "UPDATE_LOCATION";
		private final String JSON_LATITUDE = "latitude";
		private final String JSON_LONGITUDE = "longitude";
		private final String JSON_DATETIME = "datetime";
		
        @Override
        protected String doInBackground(Double... params)
        {
        	// Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(SERVER_URL);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            JSONObject updateLocJSON = new JSONObject();
			try
			{
				//Build json object
				updateLocJSON.put(JSON_COMMAND, JSON_UPDATE_LOCATION_COMMAND);
				updateLocJSON.put(JSON_LATITUDE, params[0]);
				updateLocJSON.put(JSON_LONGITUDE, params[1]);
				updateLocJSON.put(JSON_DATETIME, System.currentTimeMillis());
					
				//Create string from it and post to server
				StringEntity se = new StringEntity(updateLocJSON.toString());
	            httpPost.setEntity(se);
	            HttpResponse response = httpClient.execute(httpPost);
	            InputStream is = response.getEntity().getContent();
	            
	            result = convertStreamToString(is);
	            
	            return result;
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
			return result;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
        	Log.i(TAG+": "+TASK_TAG, result);
        }
    }
	
	private static String convertStreamToString(InputStream is)
    {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
    }
	
}
