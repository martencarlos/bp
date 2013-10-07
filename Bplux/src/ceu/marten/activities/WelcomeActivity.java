package ceu.marten.activities;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

public class WelcomeActivity extends Activity {

	
	Dialog dialog;
	ArrayList<BPDevice> devices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);
		
		
		dialog = new Dialog(this);
		dialog.setTitle("details of device");
		dialog.setContentView(R.layout.activity_device_details);
		
		final OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
			public void onItemClick(
					@SuppressWarnings("rawtypes") AdapterView parent, View v,
					int position, long id) {
				
				BPDevice dev = new BPDevice();
				dev = devices.get(position);
				((TextView) dialog.findViewById(R.id.name)).setText(dev.getName());
				((TextView) dialog.findViewById(R.id.channel)).setText("channel "+Integer.toString(dev.getChannel()));
				((TextView) dialog.findViewById(R.id.frequency)).setText(Float.toString(dev.getFreq()));
				((TextView) dialog.findViewById(R.id.bitsSignal)).setText(Integer.toString(dev.getnBits()));
				if(dev.isDigOutput())
					((TextView) dialog.findViewById(R.id.digOutput)).setText("yes");
				else
					((TextView) dialog.findViewById(R.id.digOutput)).setText("no");
				if(dev.isSimDevice())
					((TextView) dialog.findViewById(R.id.devType)).setText("simulated device");
				else
					((TextView) dialog.findViewById(R.id.devType)).setText("physical device");
				
				dialog.show();
			}
		};
		ListView listView = (ListView) findViewById(R.id.lvDevices);
		listView.setOnItemClickListener(mMessageClickedHandler);
		
		
		/*READ DEVICES FROM INTERNAL STORAGE*/
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		devices = new ArrayList<BPDevice>();
		
		File file = getBaseContext().getFileStreamPath("devices");
		if(file.exists()){
		try {
			 fis= openFileInput("devices");
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
		
		while(true)
		try {
			devices.add((BPDevice)ois.readObject());
		} catch (EOFException e)
		{break;}
		catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ListView lv = new ListView(getApplicationContext());
		lv = (ListView) findViewById(R.id.lvDevices);
        ArrayList<String> deviceNames = new ArrayList<String>();
        
       for (BPDevice b: devices)
        	deviceNames.add(b.getName());
        
        
        ArrayAdapter<String> arrayAdapter =      
        new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, deviceNames);
        lv.setAdapter(arrayAdapter);
		}
		
		
		
		/*
		Intent intent = getIntent();
		if(intent.hasExtra("deviceSettings")){
			
			newDevice = (BPDevice) intent.getSerializableExtra("deviceSettings");
			ListView lv = new ListView(getApplicationContext());
			lv = (ListView) findViewById(R.id.lvDevices);
	        ArrayList<String> list = new ArrayList<String>();
	        list.add(newDevice.getName());
	        ArrayAdapter<String> arrayAdapter =      
	        new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list);
	        lv.setAdapter(arrayAdapter); 
		}
		*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}

	public void launchGraph(View v) {
		Intent intent = new Intent(WelcomeActivity.this, GraphActivity.class);
		startActivity(intent);
	}
	
	public void onClickedNewDevice(View v) {
		Intent intent = new Intent(WelcomeActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
	

}
