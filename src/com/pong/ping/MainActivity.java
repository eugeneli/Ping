package com.pong.ping;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import com.pong.ping.models.User;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends FragmentActivity implements PingActivity
{
	private final String TAG = getClass().getSimpleName();
	private PingPagerAdapter pagerAdapter;
	List<Fragment> fragments = new Vector<Fragment>();
	
	private User user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
		setContentView(R.layout.activity_main);
		
		user = new User();

		initializePaging();
		scheduleUpdateService();
	}
	
	private void initializePaging()
	{	
		Fragment googleMapFragment = Fragment.instantiate(this, MapFragment.class.getName());
		Fragment googleMapFragment2 = Fragment.instantiate(this, MapFragment.class.getName());
		
		fragments.add(googleMapFragment);
		fragments.add(googleMapFragment2);
		/*fragments.add(Fragment.instantiate(this, WaspitHomeFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, SplitBillFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, SendMoneyFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, RequestMoneyFragment.class.getName()));*/
		pagerAdapter  = new PingPagerAdapter(super.getSupportFragmentManager(), fragments);

		ViewPager pager = (ViewPager)super.findViewById(R.id.viewpager);
		pager.setAdapter(pagerAdapter);
	}
	
	//TODO: !
	private void scheduleUpdateService()
	{
		//Check if location is enabled
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	    {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("Your location must be available to receive pings!")
	               .setCancelable(false)
	               .setPositiveButton("Open location settings", new DialogInterface.OnClickListener()
	               {
	                   public void onClick(final DialogInterface dialog, final int id)
	                   {
	                       startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                       finish();
	                   }
	               })
	               .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	               {
	                   public void onClick(final DialogInterface dialog, final int id)
	                   {
	                        dialog.cancel();
	                        finish();
	                   }
	               });
	        final AlertDialog alert = builder.create();
	        alert.show();
	    }
	    else //Schedule service to start
	    {
	    	/*
	    	Intent intent = new Intent(this, ETAUpdateService.class);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
			
			AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), UPDATE_INTERVAL, pendingIntent);
			*/
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class PingPagerAdapter extends FragmentPagerAdapter
	{
		private List<Fragment> fragments;

		public PingPagerAdapter(FragmentManager fm, List<Fragment> fragments)
		{
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position)
		{
			return this.fragments.get(position);
		}

		@Override
		public int getCount()
		{
			return this.fragments.size();
		}
	}

	@Override
	public User getUser()
	{
		return user;
	}

	@Override
	public void setUser(User user)
	{
		this.user = user;
	}

}
