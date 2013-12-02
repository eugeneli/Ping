package com.cs9033.ping;

import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity implements PingActivity  {
	private static final String fragClasses = "FRAGMENT_CLASSES";
	private static final String fragStates = "FRAGMENT_STATES";
	private static final String currUser = "CURRENT_USER";
	private static final String currLoc = "CURRENT_LOCATION";
	private static final String currLat = "CURRENT_LATITUDE";
	private static final String currLong = "CURRENT_LONGITUDE";
	
	private Stack<String> fragmentClasses = new Stack<String>();
	private Stack<Fragment.SavedState> fragmentStates = new Stack<Fragment.SavedState>();
	private LocationClient lc;
	public static final int GOOGLE_MAPS_FIX_CONNECTION = 9001;
	
	private User currentUser;
	private LatLng currentLocation;
	
	private ConnectionCallbacks conn = new ConnectionCallbacks() {
		@Override
		public void onConnected(Bundle arg0) {
			lc.requestLocationUpdates(new LocationRequest().setInterval(30000), ll);
		}
		@Override
		public void onDisconnected() {
		}
	};
	private OnConnectionFailedListener fail = new OnConnectionFailedListener() {

		@Override
		public void onConnectionFailed(ConnectionResult result) {
			if (result.hasResolution())
				try {
					result.startResolutionForResult(MainActivity.this, GOOGLE_MAPS_FIX_CONNECTION);
				} catch (SendIntentException e) {
					e.printStackTrace();
				}
			
		}
	};
	
	private LocationListener ll = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
			MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
			if (fragment != null)
				fragment.updateLocation(currentLocation);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState != null) {
			String[] classes = savedInstanceState.getStringArray(fragClasses);
			Fragment.SavedState[] states = (SavedState[]) savedInstanceState.getParcelableArray(fragStates);
			for (int i = 0; i < classes.length && i < states.length; ++i) {
				fragmentClasses.push(classes[i]);
				fragmentStates.push(states[i]);
			}
			currentLocation = savedInstanceState.getParcelable(currLoc);
			try {
				String usr = savedInstanceState.getString(currUser);
				if (usr != null)
					currentUser = new User(new JSONObject(savedInstanceState.getString(currUser)));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				SharedPreferences storage = getPreferences(0);
				if (storage.contains(currLat) && storage.contains(currLong))
					currentLocation = new LatLng(storage.getFloat(currLat, 0), storage.getFloat(currLong, 0));
				String usr = storage.getString(currUser, null);
				if (usr != null)
					currentUser = new User(new JSONObject(usr));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (fragmentClasses.empty() || fragmentStates.empty())
			loadView(MainFragment.TAG, null);
		else
			loadView(fragmentClasses.pop(), fragmentStates.pop());
		
		lc = new LocationClient(this, conn, fail);
	}	
	
	@Override
	protected void onStart() {
		super.onStart();
		lc.connect();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		fragmentClasses.pop();
		fragmentStates.pop();
	}
	
	@Override
	protected void onStop() {
		if (lc.isConnected())
			lc.removeLocationUpdates(ll);
		lc.disconnect();
		SharedPreferences.Editor prefs = getPreferences(0).edit();
		if (currentLocation != null)
			prefs.putFloat(currLat, (float)currentLocation.latitude).putFloat(currLong, (float)currentLocation.longitude);
		prefs.commit();
		super.onStop();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == GOOGLE_MAPS_FIX_CONNECTION)
			lc.connect();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		FragmentManager fm = getSupportFragmentManager();
		Fragment frag = fm.findFragmentById(R.id.frame);
		if (frag == null)
			return;
		fragmentClasses.push(frag.getTag());
		fragmentStates.push(fm.saveFragmentInstanceState(frag));
		outState.putStringArray(fragClasses, fragmentClasses.toArray(new String[fragmentClasses.size()]));
		outState.putParcelableArray(fragStates, fragmentStates.toArray(new Fragment.SavedState[fragmentStates.size()]));
		outState.putParcelable(currLoc, currentLocation);
		try {
			if (currentUser != null)
				outState.putString(currUser, currentUser.toJSON().toString());
			else
				outState.putString(currUser, null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if (fragmentClasses.empty() || fragmentStates.empty())
			finish();
		else
			loadView(fragmentClasses.pop(), fragmentStates.pop());
	}

	public void loadView(String tag) {
		saveCurrentState();
		loadView(tag, null);
	}

	private void saveCurrentState() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment oldF = fm.findFragmentById(R.id.frame);
		if (oldF != null) {
			fragmentClasses.push(oldF.getTag());
			fragmentStates.push(fm.saveFragmentInstanceState(oldF));
		}
	}
	
	private void loadView(String tag, Fragment.SavedState state) {
		Fragment newF = getFragment(tag, state);
		
		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);

		//clear focus to hide the keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(frame.getWindowToken(), 0);

		((ProgressBar) findViewById(R.id.loading)).setVisibility(View.VISIBLE);
		frame.setVisibility(View.INVISIBLE);
		
		
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction()
			.replace(R.id.frame, newF, tag)
			.commit();				
	}

	private Fragment getFragment(String tag, Fragment.SavedState state) {
		Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
		if (frag != null) return frag;
		if (tag == MainFragment.TAG) frag = new MainFragment();
		if (tag == LoginFragment.TAG) frag = new LoginFragment();
		if (tag == CreatePingFragment.TAG) frag = new CreatePingFragment();
		if (frag != null) {
			frag.setInitialSavedState(state);
		}
		return frag;
	}
	
	@Override
	public void onFragmentLoaded(Fragment fragment) {
		((ProgressBar) findViewById(R.id.loading)).setVisibility(View.INVISIBLE);
		((FrameLayout) findViewById(R.id.frame)).setVisibility(View.VISIBLE);
		fragment.setUserVisibleHint(true);
	}

	@Override
	public User getCurrentUser() {
		return currentUser;
	}
	@Override
	public void setCurrentUser(User user) {
		currentUser = user;
		SharedPreferences.Editor storage = getPreferences(0).edit();
		if (currentUser != null) {
			try {
				storage.putString(currUser, currentUser.toJSON().toString());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		storage.commit();
	}
	@Override
	public LatLng getCurrentLocation() {
		return currentLocation;
	}
}
