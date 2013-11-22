package com.cs9033.ping;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoginFragment extends Fragment {
	public static final String TAG = "LoginFragment";
	private OnFragmentLoadedListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnFragmentLoadedListener)
			listener = (OnFragmentLoadedListener)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_login, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listener.onFragmentLoaded(this);
	}
}
