package com.pong.ping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pong.ping.models.Ping;
import com.pong.ping.models.User;
import com.pong.ping.util.PingServer;
import com.pong.ping.util.PingServer.OnResponseListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapFragment extends Fragment
{
	private final String TAG = "MapFragment";
	private SupportMapFragment fragment;
	private PingActivity activity;
	
	private User user;
	private final double USER_RADIUS = 10000; //Hardcoding the radius now.
	private String currentHashtag = null;
	
	//GoogleMap-related variables
	private GoogleMap map;
	private UiSettings settings;
	private double currentZoom = -1;
	private final float DEFAULT_ZOOM = 12;
	
	private String openMarker = null;
	private Map<String, MapPing> mapPings;
	private Map<String, String> markerIdToId;
	
	private class MapPing
	{
		public Marker marker;
		public Circle circle;
		public MapPing(Marker marker, Circle circle)
		{
			this.marker = marker;
			this.circle = circle;
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
            return null;	
		return (RelativeLayout)inflater.inflate(R.layout.fragment_map, container, false);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		if (activity instanceof PingActivity)
			this.activity = (PingActivity)activity;
		
		user = this.activity.getUser();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
	    super.onActivityCreated(savedInstanceState);
	    FragmentManager fm = getChildFragmentManager();
	    fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
	    if (fragment == null) {
	        fragment = SupportMapFragment.newInstance();
	        fm.beginTransaction().replace(R.id.map, fragment).commit();
	    }  
	    
	    mapPings = new HashMap<String, MapPing>();
		markerIdToId = new HashMap<String, String>();

	    //Create map
	    if(map != null)
	    {
	    	currentZoom = map.getCameraPosition().zoom;
		    
		    settings = map.getUiSettings();
	        settings.setCompassEnabled(true);
	        settings.setMyLocationButtonEnabled(true);
	        settings.setZoomControlsEnabled(false);
	        map.setMyLocationEnabled(true);
	        map.setOnCameraChangeListener(new OnCameraChangeListener() {
					@Override
					public void onCameraChange(CameraPosition pos) {
				        if (pos.zoom != currentZoom) {
	                        currentZoom = pos.zoom;
	                }
	        }
	        });

			map.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					if (marker.getId().equals(openMarker)) {
						String id = markerIdToId.get(marker.getId());
						Bundle bundle = new Bundle();
						bundle.putString(Ping.JSON_SERVER_ID, id);
						//activity.loadView(ViewPingFragment.TAG, bundle); TODO: VIEWPINGFRAGMENT!
						return true;
					}
					openMarker = marker.getId();
					return false;
				}
				
			});
	    }
	    
	    //Get location and pings
	    LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
	    LocationListener locationListener = new UserLocationListener();  
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
	}
	
	private class UserLocationListener implements LocationListener
	{
	    @Override
	    public void onLocationChanged(Location loc)
	    {
	        //server.startUpdateLocationTask(loc.getLatitude(), loc.getLongitude());
	    	
	    	//Update location and zoom of map
	    	//TODO: Get nearby pings and display markers
	    	LatLng locCoords = new LatLng(loc.getLatitude(), loc.getLongitude());
	    	
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locCoords, DEFAULT_ZOOM);
			map.animateCamera(cameraUpdate);
			
			user.setLocation(locCoords);
			getAllPings();
	    }

	    @Override
	    public void onProviderDisabled(String provider) {}

	    @Override
	    public void onProviderEnabled(String provider) {}

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	}
	
	public ArrayList<Ping> getPingsFromJSON(JSONArray array) throws JSONException
	{
		ArrayList<Ping> pings = new ArrayList<Ping>();
		for (int n = 0; n < array.length(); n++)
			pings.add(new Ping(array.getJSONObject(n)));
		return pings;
	}
	
	public void addOrUpdatePing(Ping ping) {
		double[] coords = ping.getCoordinates();
		LatLng latlng = new LatLng(coords[0], coords[1]);
		
		if (mapPings.containsKey(ping.getServerID())) {
			MapPing mapPing = mapPings.get(ping.getServerID());
			mapPing.marker.setPosition(latlng);
			mapPing.circle.setCenter(latlng);
			mapPing.circle.setRadius(ping.getRating());
		}
		else {
			Marker marker = map.addMarker(new MarkerOptions().position(latlng).title(ping.getMessage()));
			markerIdToId.put(marker.getId(), ping.getServerID());
			Circle circle = map.addCircle(new CircleOptions()
				.center(latlng)
				.radius(ping.getRating() > -1 ? ping.getRating() : 0)
				.strokeColor(Color.GRAY)
				.strokeWidth(2.0f));
			mapPings.put(ping.getServerID(), new MapPing(marker, circle));
		}
	}
	
	public void removePing(String id) {
		if (!mapPings.containsKey(id))
			return;
		MapPing mapPing = mapPings.get(id);
		String markerId = mapPing.marker.getId();
		mapPing.circle.remove();
		mapPing.marker.remove();
		mapPings.remove(id);
		markerIdToId.remove(markerId);
	}
	
	public void getAllPings()
	{
		PingServer server = new PingServer();
		server.startGetPingsTask(user.getLocation().latitude, user.getLocation().longitude, USER_RADIUS, currentHashtag, new OnResponseListener(){
			@Override
			public void onResponse(JSONObject response) throws JSONException
			{
				Log.e(TAG, response.toString());
				if(response.getString(PingServer.ASYNC_RESPONSE_STATUS).equals(PingServer.ASYNC_NO_PINGS_FOUND))
					Toast.makeText(getActivity(), "No nearby Pings found", Toast.LENGTH_SHORT).show();
				else if (!response.getString(PingServer.ASYNC_RESPONSE_STATUS).equals(PingServer.ASYNC_SUCCESS))
					Toast.makeText(getActivity(), "Could not get pings", Toast.LENGTH_SHORT).show();
				else
				{
					JSONArray array = response.getJSONArray(PingServer.ASYNC_RESPONSE_CONTENT);
					ArrayList<Ping> pings = getPingsFromJSON(array);
					Set<String> deadPings = new HashSet<String>(mapPings.keySet());
					deadPings.removeAll(pings);
					for (String id : deadPings)
						removePing(id);
					for (Ping ping : pings)
						addOrUpdatePing(ping);
					
					/*ArrayList<String> list = new ArrayList<String>();
					for (String id: mapPings.keySet())
						list.add(id);
					SharedPreferences pref = getActivity().getSharedPreferences("file", 0);
					SharedPreferences.Editor edit = pref.edit();
					if (pref.contains("ids"))
						edit.remove("ids");
					HashSet<String> hash = new HashSet<String>(list);
					edit.putStringSet("ids", hash);
					edit.commit();*/
				}
			}
		});
		
	}

	@Override
	public void onResume()
	{
	    super.onResume();
	    if (map == null) {
	        map = fragment.getMap();
	        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
	    }
	}
}
