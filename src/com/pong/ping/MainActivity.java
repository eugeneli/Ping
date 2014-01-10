package com.pong.ping;

import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends FragmentActivity
{
	private final String TAG = getClass().getSimpleName();
	private PingPagerAdapter pagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //remove title bar
		setContentView(R.layout.activity_main);
		this.initializePaging();
	}
	
	private void initializePaging()
	{
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, MapFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, MapFragment.class.getName()));
		/*fragments.add(Fragment.instantiate(this, WaspitHomeFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, SplitBillFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, SendMoneyFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, RequestMoneyFragment.class.getName()));*/
		pagerAdapter  = new PingPagerAdapter(super.getSupportFragmentManager(), fragments);

		ViewPager pager = (ViewPager)super.findViewById(R.id.viewpager);
		pager.setAdapter(pagerAdapter);
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

}
