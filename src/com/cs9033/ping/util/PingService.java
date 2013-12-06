package com.cs9033.ping.util;

import java.util.ArrayList;

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
	final Context c = this.getBaseContext();
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
					else {
						NotificationCompat.Builder mBuilder =
						        new NotificationCompat.Builder(c)
						        //.setSmallIcon()
						        .setContentTitle("New Pings")
						        .setContentText("You have new pings");
						// Creates an explicit intent for an Activity in your app
						Intent resultIntent = new Intent(c, MainActivity.class);

						// The stack builder object will contain an artificial back stack for the
						// started Activity.
						// This ensures that navigating backward from the Activity leads out of
						// your application to the Home screen.
						TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
						// Adds the back stack for the Intent (but not the Intent itself)
						stackBuilder.addParentStack(MainActivity.class);
						// Adds the Intent that starts the Activity to the top of the stack
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent =
						        stackBuilder.getPendingIntent(
						            0,
						            PendingIntent.FLAG_UPDATE_CURRENT
						        );
						mBuilder.setContentIntent(resultPendingIntent);
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
