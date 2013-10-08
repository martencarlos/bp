package ceu.marten.activities;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

public class DevicesListAdapter extends ArrayAdapter<BPDevice> {

	private final Context context;
	private final ArrayList<BPDevice> devices;

	public DevicesListAdapter(Context context, ArrayList<BPDevice> values) {
		super(context, R.layout.device_list_item, values);
		this.context = context;
		this.devices = values;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.device_list_item, parent,
				false);

		TextView name = (TextView) rowView.findViewById(R.id.dli_name);
		TextView status = (TextView) rowView.findViewById(R.id.dli_status);

		BPDevice device = new BPDevice();
		device = devices.get(position);

		name.setText(device.getName());
		if (device.isConnected()) {
			status.setText("connected");
		} else
			status.setText("");
		return rowView;
	}
}