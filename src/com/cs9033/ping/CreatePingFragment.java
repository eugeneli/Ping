package com.cs9033.ping;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.ping.models.Ping;
import com.cs9033.ping.util.PingServer;
import com.cs9033.ping.util.PingServer.OnResponseListener;
import com.cs9033.ping.util.SerializableBitmap;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class CreatePingFragment extends Fragment {
	public static final String TAG = "CreatePingFragment";
	private static final int CAMERA_TAKE_PHOTO = 1337;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_create, container, false);
		
		if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			((ImageButton)view.findViewById(R.id.camera)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
					startActivityForResult(i, CAMERA_TAKE_PHOTO);
				}
			});
		}
		
		final EditText message = (EditText) view.findViewById(R.id.message);
		((Button)view.findViewById(R.id.submit)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng coords = activity.getCurrentLocation();
				ping.setCoordinates(coords.latitude, coords.longitude);
				
				Time now = new Time();
				now.setToNow();
				ping.setCreationDate(now.toMillis(false));
				
				ping.setMessage(message.getText().toString());
				
				PingServer ps = new PingServer();
				ps.startCreatePingTask(activity.getCurrentUser(), ping, new OnResponseListener() {
					@Override
					public void onResponse(JSONObject response)
							throws JSONException {
						if (!response.getString(PingServer.ASYNC_RESPONSE_MESSAGE).equals(PingServer.ASYNC_SUCCESS))
							Toast.makeText(getActivity(), "Could not create ping", Toast.LENGTH_SHORT).show();
						else {
							ping = null;
							activity.loadView(MainFragment.TAG);
						}
					}
				});
			}
		});
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (ping == null)
			ping = new Ping(activity.getCurrentUser());
		activity.onFragmentLoaded(this);
	}
	
	private File getTempFile() {
		return new File(Environment.getExternalStorageDirectory(), "pingimage.jpg");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_TAKE_PHOTO) {
			File tempFile = getTempFile();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bmp = BitmapFactory.decodeFile(tempFile.getPath(), options);
			if (bmp != null) {
				Toast.makeText(getActivity(), "Photo added", Toast.LENGTH_SHORT).show();
				ping.setImage(new SerializableBitmap(bmp));
			}
			else
				Toast.makeText(getActivity(), "Could not add photo", Toast.LENGTH_SHORT).show();
			tempFile.delete();			
		}
	}
}
