package com.cs9033.ping;

import java.util.Stack;

import com.cs9033.ping.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity implements PingActivity  {
	private static final String fragClasses = "FRAGMENT_CLASSES";
	private static final String fragStates = "FRAGMENT_STATES";
	
	private Stack<String> fragmentClasses = new Stack<String>();
	private Stack<Fragment.SavedState> fragmentStates = new Stack<Fragment.SavedState>();
	private LocationClient lc;
	private int FIX_SHIT = 9001;
	
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
					result.startResolutionForResult(MainActivity.this, FIX_SHIT);
				} catch (SendIntentException e) {
					e.printStackTrace();
				}
			
		}
	};
	
	private LocationListener ll = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
			if (fragment != null)
				fragment.updateLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));
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
	protected void onStop() {
		if (lc.isConnected())
			lc.removeLocationUpdates(ll);
		lc.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == FIX_SHIT)
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

		((ProgressBar) findViewById(R.id.loading)).setVisibility(View.VISIBLE);
		((FrameLayout) findViewById(R.id.frame)).setVisibility(View.INVISIBLE);

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

	
	private User currentUser;
	@Override
	public User getCurrentUser() {
		return currentUser;
	}

	@Override
	public void setCurrentUser(User user) {
		currentUser = user;
	}
	
}
