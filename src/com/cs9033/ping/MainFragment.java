package com.cs9033.ping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.SearchDialog.OnSearchListener;
import com.cs9033.ping.models.Ping;
import com.cs9033.ping.util.PingServer;
import com.cs9033.ping.util.PingServer.OnResponseListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainFragment extends Fragment {
	public static final String TAG = "MainFragment";
	private PingActivity activity;
	
	private class MapPing { //javaaaaaaa y u no tuple
		public Marker marker;
		public Circle circle;
		public MapPing(Marker marker, Circle circle) {
			this.marker = marker;
			this.circle = circle;
		}
	}

	private boolean posWasSaved = false;
	private double currentZoom = -1;
	private LatLng userLoc;
	private double userRadius = 1;
	private Circle myCircle;
	private Map<String, MapPing> mapPings;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PingActivity)
			this.activity = (PingActivity)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapPings = new HashMap<String, MapPing>();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("myloc", getMap().getCameraPosition());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		if (activity.getCurrentUser() == null)
			view.findViewById(R.id.ping_create).setVisibility(View.INVISIBLE);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final CameraPosition pos;
		if (savedInstanceState != null && savedInstanceState.containsKey("myloc"))
			pos = (CameraPosition) savedInstanceState.get("myloc");
		else if (activity.getCurrentLocation() != null)
			pos = CameraPosition.fromLatLngZoom(activity.getCurrentLocation(), 15);
		else
			pos = null;
		
		getChildFragmentManager()
			.beginTransaction()
			.add(R.id.map, SupportMapFragment.newInstance(), "Map")
			.commit();
		getChildFragmentManager().executePendingTransactions();
		
		final MainFragment self = this;
		getView().post(new Runnable() {
			@Override
			public void run() {
				final GoogleMap map = getMap();
				if (pos != null) {
					posWasSaved = true;
					map.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
				}
				
				currentZoom = map.getCameraPosition().zoom;
				userRadius = 5 * Math.pow(2, map.getMaxZoomLevel() - currentZoom);
				myCircle = map.addCircle(new CircleOptions()
					.center(userLoc != null ? userLoc : new LatLng(0, 0))
					.radius(userRadius)
					.fillColor(getResources().getColor(R.color.see_thru_lighter_blue))
					.strokeColor(Color.GRAY)
					.strokeWidth(2.0f));
				UiSettings settings = map.getUiSettings();
				settings.setCompassEnabled(true);
				settings.setMyLocationButtonEnabled(true);
				settings.setZoomControlsEnabled(false);
				map.setMyLocationEnabled(true);
				map.setOnCameraChangeListener(new OnCameraChangeListener() {
					@Override
					public void onCameraChange(CameraPosition pos) {
						if (pos.zoom != currentZoom) {
							currentZoom = pos.zoom;
							userRadius = 5 * Math.pow(2, map.getMaxZoomLevel() - currentZoom);
							updateRadius(userRadius);
						}
					}
				});
				if (activity != null)
					activity.onFragmentLoaded(self);
			}
		});
				
		((Button) getView().findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.loadView(LoginFragment.TAG);
			}
		});
		((Button) getView().findViewById(R.id.ping_create)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.loadView(CreatePingFragment.TAG);
			}
		});
		((ImageButton) getView().findViewById(R.id.ping_search)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchDialog sd = new SearchDialog();
				sd.setListener(new OnSearchListener() {
					@Override
					public void onSearch(String tag) {
						String dummy = "{\"message\":\"Test ping\",\"has_image\":1,\"create_date\":1386316700000,\"longitude\":-73.9802255,\"ping_id\":\"52a183abcf935\",\"rating\":0,\"latitude\":40.6922005,\"creator_id\":\"52980ecae75b8\"}";
						try {
							addOrUpdatePing(new Ping(new JSONObject(dummy)));
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
						
						//Eugene plz wake up
						/*PingServer server = new PingServer();
						server.startGetPingsTask(userLoc.latitude, userLoc.longitude, tag, new OnResponseListener(){
							@Override
							public void onResponse(JSONObject response)
									throws JSONException {
								if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0)
									Toast.makeText(getActivity(), "Could not get pings", Toast.LENGTH_SHORT).show();
								else {
									JSONArray array = response.getJSONArray(PingServer.ASYNC_RESULT);
									ArrayList<Ping> pings = getPingsFromJSON(array);
									for (Ping ping : pings)
										addOrUpdatePing(ping);
									ArrayList<String> list = new ArrayList<String>();
									for (String id: mapPings.keySet())
										list.add(id);
									SharedPreferences pref = MainFragment.this.getActivity().getSharedPreferences("file", 0);
									SharedPreferences.Editor edit = pref.edit();
									if (pref.contains("ids"))
										edit.remove("ids");
									HashSet<String> hash = new HashSet<String>(list);
									edit.putStringSet("ids", hash);
								}
							}
						});*/
					}
				});
				sd.show(getChildFragmentManager(), "SearchDialog");
			}
		});
	}
	
	public void updateRadius(double radius) {
		myCircle.setRadius(radius);
	}
	
	public void updateLocation(LatLng location) {
		if (userLoc == null && !posWasSaved)
			getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
		userLoc = location;
		if (myCircle != null)
			myCircle.setCenter(userLoc);
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
			GoogleMap map = getMap();
			Marker marker = map.addMarker(new MarkerOptions().position(latlng).title(ping.getMessage()));
			Circle circle = map.addCircle(new CircleOptions()
				.center(latlng)
				.radius(ping.getRating() + 1)
				.strokeColor(Color.GRAY)
				.strokeWidth(2.0f));
			mapPings.put(ping.getServerID(), new MapPing(marker, circle));
		}
	}
	
	private SupportMapFragment getMapFragment() {
		FragmentManager fm = getChildFragmentManager();
		return (SupportMapFragment) fm.findFragmentById(R.id.map);		
	}
	private GoogleMap getMap() {
		return getMapFragment().getMap();
	}
	
	public ArrayList<Ping> getPingsFromJSON(JSONArray array) throws JSONException
	{
		ArrayList<Ping> pings = new ArrayList<Ping>();
		for (int n = 0; n < array.length(); n++)
			pings.add(new Ping(array.getJSONObject(n)));
		return pings;
	}
	
	public void getAllPings()
	{
		String dummy = "{\"message\":\"Test ping\",\"has_image\":1,\"create_date\":1386316700000,\"longitude\":-73.9802255,\"ping_id\":\"52a183abcf935\",\"rating\":0,\"latitude\":40.6922005,\"creator_id\":\"52980ecae75b8\"}";
		try {
			addOrUpdatePing(new Ping(new JSONObject(dummy)));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		//Eugene plz wake up
		/*PingServer server = new PingServer();
		server.startGetPingsTask(userLoc.latitude, userLoc.longitude, new OnResponseListener(){
			@Override
			public void onResponse(JSONObject response)
					throws JSONException {
				if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0)
					Toast.makeText(getActivity(), "Could not get pings", Toast.LENGTH_SHORT).show();
				else {
					JSONArray array = response.getJSONArray(PingServer.ASYNC_RESULT);
					ArrayList<Ping> pings = getPingsFromJSON(array);
					for (Ping ping : pings)
						addOrUpdatePing(ping);
					ArrayList<String> list = new ArrayList<String>();
					for (String id: mapPings.keySet())
						list.add(id);
					SharedPreferences pref = MainFragment.this.getActivity().getSharedPreferences("file", 0);
					SharedPreferences.Editor edit = pref.edit();
					if (pref.contains("ids"))
						edit.remove("ids");
					HashSet<String> hash = new HashSet<String>(list);
					edit.putStringSet("ids", hash);
				}
			}
		});*/
		
	}

}
