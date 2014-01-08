package ceu.marten.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import ceu.marten.bplux.R;

public class ChannelsToDisplayListAdapter extends ArrayAdapter<String>
		implements OnCheckedChangeListener {

	private final Context context;
	private boolean[] channelsToDisplay;
	private ArrayList<String> channels;
	private ArrayList<String> sensors;
	private boolean[] checkedStatus;

	public ChannelsToDisplayListAdapter(Context context,
			ArrayList<String> channels, ArrayList<String> sensors) {
		super(context, 0, 0, channels);
		this.context = context;
		this.channels = channels;
		this.sensors = sensors;
		this.checkedStatus = new boolean[channels.size()];
		this.channelsToDisplay = new boolean[channels.size()];
		for (int i = 0; i < channelsToDisplay.length; i++)
			channelsToDisplay[i] = false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.li_channels_to_display, parent, false);

		}

		// GET VIEWS
		TextView channelNumber = (TextView) rowView
				.findViewById(R.id.li_ctd_channelNumber);
		TextView sensor = (TextView) rowView.findViewById(R.id.li_ctd_sensor);
		CheckBox cb = (CheckBox) rowView.findViewById(R.id.li_ctd_checkbox);

		// SETUP CHECK BOX
		cb.setTag(R.id.TAG_POSITION, position);
		cb.setOnCheckedChangeListener(this);
		if(checkedStatus[position] == true) {
			 cb.setChecked(true);
			 } else {
				 cb.setChecked(false);
			}
		// SETUP CHANNEL NUMBER
		channelNumber.setText(channels.get(position));

		// SETUP SENSOR
		sensor.setText(sensors.get(position));

		return rowView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
			checkedStatus[position]=true;
			channelsToDisplay[position] = true;

		} else {
			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
			checkedStatus[position] = false;
			channelsToDisplay[position] = false;
		}
	}

	public boolean[] getChecked() {
		return channelsToDisplay;
	}
}
