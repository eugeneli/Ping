package com.cs9033.ping;

import com.cs9033.ping.models.User;
import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface PingActivity {
	public void onFragmentLoaded(Fragment fragment);
	public void loadView(String tag);
	public void loadView(String tag, Bundle args);
	
	public User getCurrentUser();
	public void setCurrentUser(User user);
	public LatLng getCurrentLocation();
}
