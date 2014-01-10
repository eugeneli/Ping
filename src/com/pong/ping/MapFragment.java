package com.pong.ping;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MapFragment extends Fragment
{
	private final String TAG = "MapFragment";
	private SupportMapFragment fragment;
	
	//GoogleMap-related variables
	private GoogleMap map;
	private UiSettings settings;
	private Circle pingCircle;
	private double currentZoom = -1;
	private LatLng userLoc;
	private double userRadius = 1;
	
	private SeekBar radiusLevel;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
            return null;	
		return (RelativeLayout)inflater.inflate(R.layout.fragment_map, container, false);
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
	    
	    if(map != null)
	    {
	    	currentZoom = map.getCameraPosition().zoom;
	        userRadius = 5 * Math.pow(2, map.getMaxZoomLevel() - currentZoom);
		    pingCircle = map.addCircle(new CircleOptions()
	        			.center(userLoc != null ? userLoc : new LatLng(0, 0))
	        			.radius(userRadius)
	        			.fillColor(getResources().getColor(R.color.see_thru_lighter_blue))
	        			.strokeColor(Color.GRAY)
	        			.strokeWidth(2.0f));
		    pingCircle.setCenter(userLoc);
		    
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
	                        userRadius = 5 * Math.pow(2, map.getMaxZoomLevel() - currentZoom);
	                        updateRadius(userRadius);
	                }
	        }
	        });
		    
		    radiusLevel = (SeekBar) getView().findViewById(R.id.slider);
		    radiusLevel.setMax(100);
		    radiusLevel.setProgress(50);
		    radiusLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() //NONE OF THESE EVENTS ARE BEING TRIGGERD?!?!?!?!
		    {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					updateRadius(progress);
					//Log.d(TAG, "rad: "+pingCircle.getRadius());
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
		    });
	    }
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
	
	public void updateRadius(double radius)
	{
        pingCircle.setRadius(radius);
        userRadius = radius;
       // System.out.println("@@@@@@@@@@@@@@@@@@"+userRadius);
    //    getAllPings();
	}
}
