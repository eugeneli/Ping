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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreatePingFragment extends Fragment {
	public static final String TAG = "CreatePingFragment";
	private PingActivity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PingActivity)
			this.activity = (PingActivity)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_create, container, false);
		final EditText message = (EditText) view.findViewById(R.id.message);
		((Button)view.findViewById(R.id.submit)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Ping ping = new Ping(activity.getCurrentUser(), activity.getCurrentLocation(), message.getText().toString(), null);
				PingServer ps = new PingServer();
				ps.startCreatePingTask(activity.getCurrentUser(), ping, new OnResponseListener() {
					@Override
					public void onResponse(JSONObject response)
							throws JSONException {
						if (response.getInt(PingServer.ASYNC_RESPONSE_CODE) == 0)
							Toast.makeText(getActivity(), "Could not create ping", Toast.LENGTH_SHORT).show();
						else
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
