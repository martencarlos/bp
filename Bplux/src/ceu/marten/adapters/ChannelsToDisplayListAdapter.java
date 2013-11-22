package ceu.marten.adapters;

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

public class ChannelsToDisplayListAdapter extends ArrayAdapter<String> implements
		OnCheckedChangeListener {

	private final Context context;
	private boolean[] channelsToDisplay;
	private ArrayList<String> channels;
	private ArrayList<String> sensors;

	public ChannelsToDisplayListAdapter(Context context, ArrayList<String> channels,ArrayList<String> sensors) {
		super(context, 0, 0, channels);
		this.context = context;
		this.channels = channels;
		this.sensors = sensors;
		this.channelsToDisplay = new boolean[channels.size()];
		for(int i=0;i<channelsToDisplay.length;i++)
			channelsToDisplay[i]=false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.li_channels_to_display, parent, false);

		}

		//GET VIEWS
		TextView channelNumber = (TextView) rowView.findViewById(R.id.li_ctd_channelNumber);
		TextView sensor = (TextView) rowView.findViewById(R.id.li_ctd_sensor);
		CheckBox cb = (CheckBox) rowView.findViewById(R.id.li_ctd_checkbox);
		
		//SETUP CHECK BOX
		cb.setTag(R.id.TAG_POSITION, position);
		cb.setOnCheckedChangeListener(this);

		//SETUP CHANNEL NUMBER
		channelNumber.setText(channels.get(position));
		
		//SETUP SENSOR
		sensor.setText(sensors.get(position));
		
		return rowView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
			channelsToDisplay[position] = true;

		} else {
			int position = (Integer) buttonView.getTag(R.id.TAG_POSITION);
			channelsToDisplay[position] = false;
		}
	}

	/*
	 * @Override public void onClick(View v) { CheckBox cb = (CheckBox)
	 * v.findViewById(R.id.li_ac_checkbox); cb.toggle(); }
	 */
	public boolean[] getChecked() {
		return channelsToDisplay;
	}
}
