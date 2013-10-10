package ceu.marten.activities;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;

public class WelcomeActivity extends Activity implements OnDismissCallback{

	private Dialog dialog;
	private ArrayList<BPDevice> devices = null;
	private ListView devListView;
	private Context welcomeActivityContext = this;
	DevicesListAdapter baseAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);
		Log.d("my tag", "paso por onCreate");

		loadDevicesFromInternalStorage();
		setupDeviceDetailsDialog();
		setupDevicesListView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}

	@Override
	protected void onStop() {
		saveDevicesInAndroidMemory();
		super.onStop();
	}

	private void saveDevicesInAndroidMemory() {

		String FILENAME = "devices";
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;

		try {
			// File file = getBaseContext().getFileStreamPath("devices");
			// if(file.exists())
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			// else
			// fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			oos = new ObjectOutputStream(fos);
			for (BPDevice device : devices) {
				oos.writeObject(device);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadDevicesFromInternalStorage() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		devices = new ArrayList<BPDevice>();
		System.out.println("cargo datos");
		File file = getBaseContext().getFileStreamPath("devices");
		if (file.exists()) {
			try {
				fis = openFileInput("devices");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				ois = new ObjectInputStream(fis);
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true)
				try {
					devices.add((BPDevice) ois.readObject());
				} catch (EOFException e) {
					break;
				} catch (OptionalDataException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		}
	}

	private void setupDeviceDetailsDialog() {

		dialog = new Dialog(this);
		dialog.setTitle("details of device");
		dialog.setContentView(R.layout.activity_device_details);
	}

	private void setupDevicesListView() {

		/** SETTING UP THE LISTENERS */
		final OnItemLongClickListener longPressListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> AdapterView, View v,
					int position, long id) {
				BPDevice dev = new BPDevice();
				dev = devices.get(position);

				((TextView) dialog.findViewById(R.id.name)).setText(dev
						.getName());

				((TextView) dialog.findViewById(R.id.channel))
						.setText("channel "
								+ Integer.toString(dev.getChannel()));
				((TextView) dialog.findViewById(R.id.frequency)).setText(Float
						.toString(dev.getFreq()) + " Hz");
				((TextView) dialog.findViewById(R.id.bitsSignal))
						.setText(Integer.toString(dev.getnBits()) + " bits");
				if (dev.isDigOutput())
					((TextView) dialog.findViewById(R.id.digOutput))
							.setText("yes");
				else
					((TextView) dialog.findViewById(R.id.digOutput))
							.setText("no");
				if (dev.isSimDevice())
					((TextView) dialog.findViewById(R.id.devType))
							.setText("simulated device");
				else
					((TextView) dialog.findViewById(R.id.devType))
							.setText("physical device");

				dialog.show();
				return false;
			}
		};
		final OnItemClickListener onListItemClickedListener = new OnItemClickListener() {
			public void onItemClick(
					@SuppressWarnings("rawtypes") AdapterView parent, View v,
					final int position, long id) {

				BPDevice dev = new BPDevice();
				dev = devices.get(position);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						welcomeActivityContext);
				builder.setTitle("Do you want to connect this device?");

				if (dev.isConnected()) {
					builder.setPositiveButton("disconnect",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									devices.get(position).setConnected(false);
									baseAdapter.remove(position);
									baseAdapter.add(devices.get(position));
								}
							});
				} else {
					builder.setPositiveButton("connect",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									disconnectOtherDeviceConnected();
									devices.get(position).setConnected(true);
									baseAdapter.remove(position);
									baseAdapter.add(devices.get(position));

								}
							});
				}

				builder.setNegativeButton("dismiss",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();

			}
		};

		devListView = (ListView) findViewById(R.id.lvDevices);
		devListView.setOnItemLongClickListener(longPressListener);
		devListView.setOnItemClickListener(onListItemClickedListener);
		baseAdapter = new DevicesListAdapter(this, devices);
		
		//setContextualUndoAdapterWithTimer();
		setSwipeToDismissAdapter();
	}

	public void disconnectOtherDeviceConnected() {
		for (BPDevice dev : devices) {
			if (dev.isConnected())
				dev.setConnected(false);
		}
	}

	private void setSwipeToDismissAdapter(){
		 SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter, this);
		 swipeAdapter.setAbsListView(devListView);
		 devListView.setAdapter(baseAdapter);
	}
	
	

	/* BUTTON EVENTS */
	public void onClickedShow(View v) {
		Intent intent = new Intent(WelcomeActivity.this, GraphActivity.class);
		startActivity(intent);
	}

	public void onClickedNewDevice(View v) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, 1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				
				baseAdapter.add((BPDevice) data
						.getSerializableExtra("deviceSettings"));
				baseAdapter.notifyDataSetChanged();
				devices.add((BPDevice) data
						.getSerializableExtra("deviceSettings"));
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}
	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			baseAdapter.remove(position);
			devices.remove(position);
		}
		Toast.makeText(this, "device removed ", Toast.LENGTH_SHORT).show();
	}

}
