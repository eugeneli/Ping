package com.cs9033.ping;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.Ping;
import com.cs9033.ping.util.PingServer;
import com.cs9033.ping.util.PingServer.OnResponseListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		return inflater.inflate(R.layout.fragment_view, container, false);
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

}
