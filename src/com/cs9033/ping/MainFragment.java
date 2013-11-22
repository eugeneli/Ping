package com.cs9033.ping;

import java.util.HashMap;
import java.util.Map;

import com.cs9033.ping.models.Ping;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends Fragment {
	public static final String TAG = "MainFragment";
	private OnFragmentLoadedListener listener;
	
	private class MapPing { //javaaaaaaa y u no tuple
		public Marker marker;
		public Circle circle;
		public MapPing(Marker marker, Circle circle) {
			this.marker = marker;
			this.circle = circle;
		}
	}

	private double currentZoom = -1;
	private double userRadius = 1;
	private Circle myCircle;
	private Map<String, MapPing> mapPings;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnFragmentLoadedListener)
			listener = (OnFragmentLoadedListener)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapPings = new HashMap<String, MapPing>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_map, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
				currentZoom = map.getCameraPosition().zoom;
				userRadius = 5 * Math.pow(2, map.getMaxZoomLevel() - currentZoom);
				myCircle = map.addCircle(new CircleOptions()
					.center(new LatLng(0, 0))
					.radius(userRadius)
					.fillColor(getResources().getColor(R.color.see_thru_lighter_blue))
					.strokeColor(Color.GRAY)
					.strokeWidth(2.0f));
				map.getUiSettings().setZoomControlsEnabled(false);
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
				if (listener != null)
					listener.onFragmentLoaded(self);
			}
		});
				
		((Button) getView().findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).changeViews(LoginFragment.TAG);
			}
		});
		((Button) getView().findViewById(R.id.ping_create)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).changeViews(CreatePingFragment.TAG);
			}
		});
	}
	
	public void updateRadius(double radius) {
		myCircle.setRadius(radius);
	}
	
	public void updateLocation(LatLng location) {
		myCircle.setCenter(location);
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
				.radius(ping.getRating())
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

}
