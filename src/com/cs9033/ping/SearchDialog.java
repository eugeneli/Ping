package com.cs9033.ping;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class SearchDialog extends DialogFragment {
	
	public static interface OnSearchListener {
		public void onSearch(String tag);
	}
	
	private OnSearchListener listener;
	
	public SearchDialog() {
	}
	
	public void setListener(OnSearchListener listener) {
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_search, parent, false);
		final EditText text = (EditText) view.findViewById(R.id.search_text);
		((ImageButton)view.findViewById(R.id.ping_search)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onSearch(text.getText().toString());
				}
				dismiss();
			}
		});
		return view;
	}
}
