package ceu.marten.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;

import com.haarman.listviewanimations.ArrayAdapter;

public class RecordingConfigListAdapter extends ArrayAdapter<Configuration> {

	private final Context context;

	public RecordingConfigListAdapter(Context context,
			ArrayList<Configuration> loadedDevices) {
		super(loadedDevices);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.li_configuration, parent, false);
		}

		TextView name = (TextView) rowView.findViewById(R.id.dli_name);
		TextView frequency = (TextView) rowView.findViewById(R.id.dli_freq);
		TextView mac = (TextView) rowView.findViewById(R.id.dli_mac);
		TextView bits = (TextView) rowView.findViewById(R.id.dli_nbits);
		TextView date = (TextView) rowView.findViewById(R.id.dli_date);
		TextView activeChannels = (TextView) rowView
				.findViewById(R.id.dli_active_channels);

		Configuration dev = getItem(position);

		name.setText(dev.getName());
		frequency.setText(String.valueOf(dev.getFrequency()) + " Hz");
		mac.setText(dev.getMacAddress());
		bits.setText(String.valueOf(dev.getNumberOfBits()) + " bits");
		date.setText(dev.getCreateDate());
		activeChannels.setText("channels active: ");

		for (int i : dev.getActiveChannels())
			activeChannels.append(" " + String.valueOf(i) + ",");
		activeChannels.setText(activeChannels.getText().toString()
				.substring(0, activeChannels.getText().length() - 1));
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