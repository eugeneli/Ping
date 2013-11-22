package com.cs9033.ping;

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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity implements OnFragmentLoadedListener  {
	private static final String lastFragTag = "LAST_FRAGMENT";
	private String lastFrag;
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
		
		String lastFrag = (savedInstanceState != null ? savedInstanceState.getString(lastFragTag) : null);
		changeViews(lastFrag != null ? lastFrag : MainFragment.TAG);
		
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
		outState.putString(lastFragTag, lastFrag);
	}

	@Override
	public void onBackPressed() {
		FragmentManager manager = getSupportFragmentManager();
		if (manager.getBackStackEntryCount() < 2)
			finish();
		else
			super.onBackPressed();
	}

	private Fragment getFragment(String tag) {
		Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
		if (frag != null) return frag;
		if (tag == MainFragment.TAG) return new MainFragment();
		if (tag == LoginFragment.TAG) return new LoginFragment();
		if (tag == CreatePingFragment.TAG) return new CreatePingFragment();
		return null;
	}
	
	public void changeViews(final String tag) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment newFrag = getFragment(tag);
		final Fragment newF = newFrag;

		Fragment oldF = fm.findFragmentByTag(lastFrag);
		if (oldF != null)
			oldF.setUserVisibleHint(false);
		
		((ProgressBar) findViewById(R.id.loading)).setVisibility(View.VISIBLE);
		((FrameLayout) findViewById(R.id.frame)).setVisibility(View.INVISIBLE);
		
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.frame, newF, tag);
					ft.addToBackStack(null).commit();				
			}
		});
		
		lastFrag = tag;
	}

	@Override
	public void onFragmentLoaded(Fragment fragment) {
		((ProgressBar) findViewById(R.id.loading)).setVisibility(View.INVISIBLE);
		((FrameLayout) findViewById(R.id.frame)).setVisibility(View.VISIBLE);
		fragment.setUserVisibleHint(true);
	}
	
}
