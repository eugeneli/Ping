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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.Ping;
import com.cs9033.ping.models.User;

import android.os.AsyncTask;
import android.util.Log;

public class PingServer
{
	private final static String TAG = "Server";
	private final static String SERVER_URL = "http://polychan.org/ping/";
	
	private final static String JSON_COMMAND = "command";
	private final static String JSON_DATA = "json_data";
	
	public final static String ASYNC_RESULT = "result";
	public final static String ASYNC_RESPONSE_CODE = "response_code";
	
	public static interface OnResponseListener {
		public void onResponse(JSONObject response) throws JSONException;
	}
	
	public PingServer() {}
	
	public void startCreateUserTask(String username, String password, OnResponseListener onResponse)
	{
		new CreateUserTask(onResponse).execute(username, password);
	}
	
	public void startLoginTask(String username, String password, OnResponseListener onResponse)
	{
		new LoginTask(onResponse).execute(username, password);
	}
	
	public void startCreatePingTask(User user, Ping ping, OnResponseListener onResponse)
	{
		new CreatePingTask(onResponse).execute(user, ping);
	}
	
	public void startUpdateLocationTask(double latitude, double longitude, OnResponseListener onResponse)
	{
		new UpdateLocationTask(onResponse).execute(latitude, longitude);
	}
	
	public void startVotePingTask(User user, Ping ping, int voteValue, OnResponseListener onResponse) {
		new VotePingTask(onResponse).execute(user, ping, voteValue);
	}
	
	public void startGetPingsTask(double latitude, double longitude, OnResponseListener onResponse) {
		new GetPingsTask(onResponse).execute(latitude, longitude);
	}
	
	
	private static class ServerTask extends AsyncTask<JSONObject, Void, String>
	{
		private String command;
		private String taskTag;
		private OnResponseListener onResponse;
		
		public ServerTask(String command, String taskTag, OnResponseListener onResponse)
		{
			this.command = command;
			this.taskTag = taskTag;
			this.onResponse = onResponse;
		}
		
		@Override
		protected String doInBackground(JSONObject... params) {
        	// Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(SERVER_URL);
            HttpGet httpGet = new HttpGet(SERVER_URL + "/" + command);
            
            //POST data. Add the command first.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair(JSON_COMMAND, command));
            
			try
			{
				//Get the passed-in JSON
				JSONObject json = params[0];
					
				//Add to POST data
				nameValuePairs.add(new BasicNameValuePair(JSON_DATA, json.toString()));

				if (command == "GetPings" || command == "GetPingInfo")
				{
					//GET to server
					HttpResponse response = httpClient.execute(httpGet);
					InputStream is = response.getEntity().getContent();
		            return convertStreamToString(is);
				}
				else
				{
					//POST to server
		            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		            HttpResponse response = httpClient.execute(httpPost);
		            InputStream is = response.getEntity().getContent();
		            return convertStreamToString(is);
				}
	            

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
			return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
        	Log.i(TAG+": "+taskTag, result);
        	
        	if(onResponse != null)
        	{
        		try {
        			onResponse.onResponse(new JSONObject(result));
        		}
        		catch (JSONException e) {
        			e.printStackTrace();
        		}
        	}
        }

	}
	
	private static class CreateUserTask extends ServerTask
	{
		private static final String TASK_TAG = "CreateUser";
		private static final String JSON_CREATE_USER_COMMAND = "CREATE_USER";
		
		public CreateUserTask(OnResponseListener onResponse) {
			super(JSON_CREATE_USER_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(String username, String password) {
			JSONObject json = new JSONObject();
			try {
				json.put(User.JSON_USER_NAME, username);
				json.put(User.JSON_USER_PWD, password);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
    }
	
	private static class LoginTask extends ServerTask
	{
		private static final String TASK_TAG = "Login";
		private static final String JSON_LOGIN_COMMAND = "LOGIN_USER";
		
		public LoginTask(OnResponseListener onResponse) {
			super(JSON_LOGIN_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(String username, String password) {
			JSONObject json = new JSONObject();
			try {
				json.put(User.JSON_USER_NAME, username);
				json.put(User.JSON_USER_PWD, password);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
    }

	private static class CreatePingTask extends ServerTask
	{
		private static final String TASK_TAG = "CreatePing";
		private static final String JSON_CREATE_PING_COMMAND = "CREATE_PING";
		
		public static final String JSON_PING_DATA = "ping_data";
		
		public CreatePingTask(OnResponseListener onResponse) {
			super(JSON_CREATE_PING_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(User user, Ping ping) {
			JSONObject json = new JSONObject();
			try {
				json.put(User.JSON_USER_ID, user.getUserID());
				json.put(User.JSON_AUTH_TOKEN, user.getAuthToken());
			
				json.put(JSON_PING_DATA,  ping.toJSON());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
	}
	
	//Successful Response: {"response_code": 0}
	private static class UpdateLocationTask extends ServerTask
	{
		private static final String TASK_TAG = "UpdateLoc";
		private static final String JSON_UPDATE_LOCATION_COMMAND = "UPDATE_LOCATION";
		
		public static final String JSON_LATITUDE = "latitude";
		public static final String JSON_LONGITUDE = "longitude";
		public static final String JSON_DATETIME = "datetime";
		
		public UpdateLocationTask(OnResponseListener onResponse) {
			super(JSON_UPDATE_LOCATION_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(double latitude, double longitude) {
			JSONObject json = new JSONObject();
			try {
				json.put(JSON_LATITUDE, latitude);
				json.put(JSON_LONGITUDE, longitude);
				json.put(JSON_DATETIME, System.currentTimeMillis());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
    }
	
	private static class VotePingTask extends ServerTask {
		private final static String TASK_TAG = "VotePing";
		private final static String JSON_VOTE_PING_COMMAND = "VOTE_PING";
		
		public final static String JSON_VOTE_VALUE = "vote_value";
		
		public VotePingTask(OnResponseListener onResponse) {
			super(JSON_VOTE_PING_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(User user, Ping ping, int voteValue) {
			JSONObject json = new JSONObject();
			try {
				json.put(User.JSON_USER_ID, user.getUserID());
				json.put(User.JSON_AUTH_TOKEN, user.getAuthToken());
				json.put(Ping.JSON_SERVER_ID, ping.getServerID());
				json.put(JSON_VOTE_VALUE, Integer.signum(voteValue)); // -1, 0, or 1
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
	}
	
	private static class GetPingsTask extends ServerTask {
		private final static String TASK_TAG = "GetPings";
		private final static String JSON_GET_PINGS_COMMAND = "GET_PINGS";
		
		public static final String JSON_LATITUDE = "latitude";
		public static final String JSON_LONGITUDE = "longitude";
		public static final String JSON_HASHTAG = "hashtag";
		
		public GetPingsTask(OnResponseListener onResponse) {
			super(JSON_GET_PINGS_COMMAND, TASK_TAG, onResponse);
		}
		
		public void execute(double latitude, double longitude) {
			JSONObject json = new JSONObject();
			try {
				json.put(JSON_LATITUDE, latitude);
				json.put(JSON_LONGITUDE, longitude);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
		}
		
		public void execute(double latitude, double longitude, String hashtag) {
			JSONObject json = new JSONObject();
			try {
				json.put(JSON_LATITUDE, latitude);
				json.put(JSON_LONGITUDE, longitude);
				json.put(JSON_HASHTAG, hashtag);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			execute(json);
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
