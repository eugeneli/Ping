package com.cs9033.ping;

import com.cs9033.ping.models.User;

import android.support.v4.app.Fragment;

public interface PingActivity {
	public void onFragmentLoaded(Fragment fragment);
	public User getCurrentUser();
	public void setCurrentUser(User user);
	public void loadView(String tag);
}
