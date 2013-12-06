package com.cs9033.ping.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.MainActivity;
import com.cs9033.ping.R;
import com.cs9033.ping.models.Ping;
import com.cs9033.ping.util.PingServer.OnResponseListener;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class PingService extends Service {
	
	

private final String TAG = "UpdateService";
	
	private String SERVER_URL;
	private PingServer server;
	final Context c = this;
	private int mId;
	
	@Override
    public void onCreate()
	{
        super.onCreate();
        SERVER_URL = "polychan.org/ping"; // change this later!!!
        server = new PingServer();
        mId = 0;
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "UpdateService started");
	    LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    LocationListener locationListener = new PingListener();  
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
	    
	    return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<Ping> getPingsFromJSON(JSONArray array) throws JSONException
	{
		ArrayList<Ping> pings = new ArrayList<Ping>();
		for (int n = 0; n < array.length(); n++)
			pings.add(new Ping(array.getJSONObject(n)));
		return pings;
	}
	
	private boolean thereAreNewPings(JSONObject response) throws JSONException
	{
		SharedPreferences pref = c.getSharedPreferences(getResources().getString(R.string.shared_pref), 0);
		Set<String> set = pref.getStringSet("ids", new HashSet<String>());
		HashSet<String> hash = new HashSet<String>(set);
		
		JSONArray array = response.getJSONArray(PingServer.ASYNC_RESULT);
		ArrayList<Ping> pings = getPingsFromJSON(array);
		HashSet<String> hash2 = new HashSet<String>();
		
		for (Ping ping : pings)
			hash2.add(ping.getServerID());
		
		if (hash.size() == hash2.size())
		{
			hash.removeAll(hash2);
			if (hash.size() == 0)
				return true;
		}
		
		return false;
	}
	
	private class PingListener implements LocationListener
	{
	    @Override
	    public void onLocationChanged(Location loc)
	    {
	    	server.startGetPingsTask(loc.getLatitude(), loc.getLongitude(), new OnResponseListener(){
				@Override
				public void onResponse(JSONObject response)
						throws JSONException {
					if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0)
						Log.d(TAG, "Couldn't get pings");
					else if (!thereAreNewPings(response))
					{
						Log.d(TAG, "No new pings");
					}
					else {
						NotificationCompat.Builder mBuilder =
						        new NotificationCompat.Builder(c)
						        //.setSmallIcon()
						        .setContentTitle("New Pings")
						        .setContentText("You have new pings");
						// Creates an explicit intent for an Activity in your app
						Intent intent = new Intent(c, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						PendingIntent pi = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_ONE_SHOT);
						mBuilder.setContentIntent(pi);
						NotificationManager mNotificationManager =
						    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
						mNotificationManager.notify(mId, mBuilder.build());
					}
				}
			});
	    }

	    @Override
	    public void onProviderDisabled(String provider) {}

	    @Override
	    public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	}
}
