package ceu.marten.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;

import com.haarman.listviewanimations.ArrayAdapter;

public class RecordingConfigListAdapter extends ArrayAdapter<Configuration> {

	private final Context context;

	public RecordingConfigListAdapter(Context context, ArrayList<Configuration> loadedDevices) {
		super(loadedDevices);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewGroup rowView = (ViewGroup) convertView;
		if (rowView == null) {
			rowView = (ViewGroup) LayoutInflater.from(context).inflate(
					R.layout.li_config, parent, false);
		}

		TextView name = (TextView) rowView.findViewById(R.id.dli_name);
		
		Configuration dev = getItem(position);
		
		name.setText(dev.getName());
		
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