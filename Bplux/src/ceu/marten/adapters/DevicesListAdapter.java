package ceu.marten.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

import com.haarman.listviewanimations.ArrayAdapter;

public class DevicesListAdapter extends ArrayAdapter<BPDevice> {

	private final Context context;

	public DevicesListAdapter(Context context, ArrayList<BPDevice> loadedDevices) {
		super(loadedDevices);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.device_list_item, parent, false);
		}

		TextView name = (TextView) rowView.findViewById(R.id.dli_name);
		TextView status = (TextView) rowView.findViewById(R.id.dli_status);
		BPDevice dev = getItem(position);

		name.setText(dev.getName());
		if (dev.isConnected()) {
			status.setText("connected");
		} else
			status.setText("");
		return rowView;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}