package com.cs9033.ping;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.Ping;
import com.cs9033.ping.util.PingServer;
import com.cs9033.ping.util.PingServer.OnResponseListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPingFragment extends Fragment {
	public static final String TAG = "VotePingFragment";

	private PingActivity activity;
	private Ping ping;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PingActivity)
			this.activity = (PingActivity)activity;
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_view, container, false);
		Button up = (Button)view.findViewById(R.id.ping_plus);
		Button down = (Button)view.findViewById(R.id.ping_minus);
		if (activity.getCurrentUser() == null)
		{
			up.setVisibility(View.GONE);
			down.setVisibility(View.GONE);
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (ping != null) {
			displayPing();
			activity.onFragmentLoaded(this);
			return;
		}
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState == null || !savedInstanceState.containsKey(Ping.JSON_SERVER_ID)) {
			getActivity().onBackPressed();
			return;
		}
		PingServer ps = new PingServer();
		ps.startGetPingInfoTask(savedInstanceState.getString(Ping.JSON_SERVER_ID), new OnResponseListener() {
			@Override
			public void onResponse(JSONObject response) throws JSONException {
				if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0) {
					Toast.makeText(getActivity(), "Failed to load ping", Toast.LENGTH_SHORT).show();
					getActivity().onBackPressed();
				}
				else {
					ping = new Ping(response.getJSONObject("pings"));
					displayPing();
					activity.onFragmentLoaded(ViewPingFragment.this);
				}
			}
		});
		
	}
	
	private void displayPing() {
		((TextView)getView().findViewById(R.id.message)).setText(ping.getMessage());
		
		ImageView image = (ImageView) getView().findViewById(R.id.image);
		if (ping.hasImage())
			image.setImageBitmap(ping.getImage().getBitmap());
		else
			image.setVisibility(View.GONE);
	}
	
	private void voteUp(View v)
	{
		PingServer ps = new PingServer();
		ps.startVotePingTask(activity.getCurrentUser(), ping, 1, new OnResponseListener(){
			@Override
			public void onResponse(JSONObject response) throws JSONException {
				if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0) {
					Toast.makeText(getActivity(), "The vote didn't register", Toast.LENGTH_SHORT).show();
				}
				else {
					ping.rateUp();
					activity.onFragmentLoaded(ViewPingFragment.this);
				}
			}
		});
	}
	
	private void voteDown(View v)
	{
		PingServer ps = new PingServer();
		ps.startVotePingTask(activity.getCurrentUser(), ping, -1, new OnResponseListener(){
			@Override
			public void onResponse(JSONObject response) throws JSONException {
				if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0) {
					Toast.makeText(getActivity(), "The vote didn't register", Toast.LENGTH_SHORT).show();
				}
				else {
					ping.rateDown();
					activity.onFragmentLoaded(ViewPingFragment.this);
				}
			}
		});
	}
	
	private void returnToMainFragment(View v)
	{
		activity.loadView(MainFragment.TAG);
	}
}
