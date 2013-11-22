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
	
	
	public void startGetTripStatusTask(long tripID)
	{
		new GetTripStatusTask().execute(tripID);
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
	
	/*Responses Example: {"distance_left": [20.399999999999999, 6.7000000000000002], "time_left": [1920, 900], "people": ["Joe Smith", "John Doe"]}
	  The response contains three lists. "people" contains the list of people being tracked for that trip. "distance_left" contains the list of miles left in the order of the "people" list. 
	  "time_left" is similar, but reports the number of seconds until arrival at the trip's destination.
	*/
	private static class GetTripStatusTask extends AsyncTask<Long, Void, String>
	{
		private final String TASK_TAG = "GetTripStatus";
		private String result = "";
		
		private final String JSON_TRIP_STATUS_COMMAND = "TRIP_STATUS";
		private final String JSON_TRIP_ID = "trip_id";

        @Override
        protected String doInBackground(Long... params)
        {
        	// Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(SERVER_URL);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            JSONObject getTripStatusJSON = new JSONObject();
            
			try
			{
				//Build json object
				getTripStatusJSON.put(JSON_COMMAND, JSON_TRIP_STATUS_COMMAND);
				getTripStatusJSON.put(JSON_TRIP_ID, params[0]);
					
				//Create string from it and post to server
				StringEntity se = new StringEntity(getTripStatusJSON.toString());
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
        	Message msg = new Message();
        	Bundle bundle = new Bundle();
        	bundle.putString(ASYNC_RESULT, result);
        	msg.setData(bundle);
        	completionHandler.dispatchMessage(msg);
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
