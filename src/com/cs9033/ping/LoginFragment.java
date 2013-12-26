package com.cs9033.ping;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.User;
import com.cs9033.ping.util.PingServer;
import com.cs9033.ping.util.PingServer.OnResponseListener;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {
	public static final String TAG = "LoginFragment";
	private PingActivity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PingActivity)
			this.activity = (PingActivity)activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);
		((TextView)view.findViewById(R.id.title)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/bauhaus93.ttf"));
		final EditText username = (EditText) view.findViewById(R.id.username);
		final EditText password = (EditText) view.findViewById(R.id.password);
		
		((Button)view.findViewById(R.id.new_user)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PingServer ps = new PingServer();
				final String name = username.getText().toString();
				final String pass = password.getText().toString();
				ps.startCreateUserTask(name, pass, new OnResponseListener() {
					@Override
					public void onResponse(JSONObject response) throws JSONException {
						if (!response.getString(PingServer.ASYNC_RESPONSE_MESSAGE).equals(PingServer.ASYNC_SUCCESS)) {
							Toast.makeText(getActivity(), "Could not create account", Toast.LENGTH_SHORT).show();
							return;	
						}
						User currentUser = new User(response);
						activity.setCurrentUser(currentUser);
						Toast.makeText(getActivity(), "Account created for " + currentUser.getName(), Toast.LENGTH_SHORT).show();
						activity.loadView(MainFragment.TAG);
					}
				});
			}
		});
		
		((Button)view.findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PingServer ps = new PingServer();
				final String name = username.getText().toString();
				final String pass = password.getText().toString();
				ps.startLoginTask(name, pass, new OnResponseListener() {
					@Override
					public void onResponse(JSONObject response) throws JSONException {
						if (!response.getString(PingServer.ASYNC_RESPONSE_MESSAGE).equals(PingServer.ASYNC_SUCCESS)) {
							Toast.makeText(getActivity(), "Could not login", Toast.LENGTH_SHORT).show();
							return;
						}
						User currentUser = new User(response);
						activity.setCurrentUser(currentUser);
						Toast.makeText(getActivity(), "Logged in as " + currentUser.getName(), Toast.LENGTH_SHORT).show();
						activity.loadView(MainFragment.TAG);
					}
				});
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.onFragmentLoaded(this);
	}
}
