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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;

public class WelcomeActivity extends Activity implements DeleteItemCallback {

	private Dialog dialog;
	private ArrayList<BPDevice> devices = null;
	private ListView devListView;
	private Context welcomeActivityContext = this;
	private boolean devicesLoaded = false;
	DevicesListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);
		Log.d("my tag", "paso por onCreate");
		if (!devicesLoaded) {
			loadDevicesFromInternalStorage();
			setupDeviceDetailsDialog();
			setupDevicesListView();
			devicesLoaded = true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}

	/*
	 * @Override protected void onStop() { saveDevicesInAndroidMemory();
	 * super.onStop(); }
	 */
	private void saveDevicesInAndroidMemory() {
		String FILENAME = "devices";
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;

		try {
			// File file = getBaseContext().getFileStreamPath("devices");
			// if(file.exists())
			fos = openFileOutput(FILENAME, Context.MODE_APPEND);
			// else
			// fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			oos = new ObjectOutputStream(fos);
			for (BPDevice device : devices) {
				oos.writeObject(device);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			oos.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (true)
				try {
					devices.add((BPDevice) ois.readObject());
				} catch (EOFException e) {
					break;
				} catch (OptionalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	/*
	 * @Override public void onDismiss(AbsListView listView, int[]
	 * reverseSortedPositions) { for (int position : reverseSortedPositions) {
	 * devices.remove(position); } Toast.makeText(this, "Removed positions: " +
	 * Arrays.toString(reverseSortedPositions), Toast.LENGTH_SHORT).show(); }
	 */

	@Override
	public void deleteItem(int position) {
		
		if(devices.size() != 1){
			devices.remove(position);
			instantiateAdapter();
		}
		else{
			//borrar adapter
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
						.toString(dev.getFreq())+" Hz");
				((TextView) dialog.findViewById(R.id.bitsSignal))
						.setText(Integer.toString(dev.getnBits())+" bits");
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
									instantiateAdapter();
								}
							});
				} else {
					builder.setPositiveButton("connect",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									disconnectOtherDeviceConnected();
									devices.get(position).setConnected(true);
									instantiateAdapter();

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

		/*
		 * SWIPE ADAPTER SwipeDismissAdapter swipeAdapter = new
		 * SwipeDismissAdapter(adapter, this);
		 * swipeAdapter.setAbsListView(devListView);
		 * devListView.setAdapter(adapter);
		 */
		instantiateAdapter();

	}
	
	public void disconnectOtherDeviceConnected(){
			for (BPDevice dev:devices){
				if(dev.isConnected())
					dev.setConnected(false);
			}
	}

	private void instantiateAdapter() {
		
		adapter = new DevicesListAdapter(this, devices);
		ContextualUndoAdapter contextualAdapter = new ContextualUndoAdapter(
				adapter, R.layout.undo_row, R.id.undo_row_undobutton,3000);

		contextualAdapter.setAbsListView(devListView);
		devListView.setAdapter(contextualAdapter);
		contextualAdapter.setDeleteItemCallback(this);
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
				devices.add((BPDevice) data
						.getSerializableExtra("deviceSettings"));
				
				instantiateAdapter();
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}

}
