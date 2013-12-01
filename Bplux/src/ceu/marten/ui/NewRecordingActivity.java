package ceu.marten.ui;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.model.Recording;
import ceu.marten.model.io.DatabaseHelper;
import ceu.marten.services.LocalService;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.jjoe64.graphview.GraphView.GraphViewData;

public class NewRecordingActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private TextView ui_recordingName, ui_configurationName, ui_bits, ui_freq,
	ui_activeChannels, ui_macAddress;
	private LinearLayout ui_graph;
	private Button ui_startStopbutton;
	

	private Configuration currentConfiguration;
	String recordingName;
	Bundle extras;
	
	Messenger mService = null;
	static int dato = 0;
	static HRGraph graph;
	boolean isServiceBounded = false;
	boolean isReceivingData = false;
	final Messenger mActivity = new Messenger(new IncomingHandler());

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LocalService.MSG_VALUE:
				dato = msg.arg1;
				appendDataToGraph();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						LocalService.MSG_REGISTER_CLIENT);
				msg.replyTo = mActivity;
				mService.send(msg);
			} catch (RemoteException e) {
				Log.d("bplux_service",
						"error: service could not be initialized");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Log.d("bplux_service", "service disconnected!");
		}
	};

	static void appendDataToGraph() {
		graph.setxValue(graph.getxValue() + 1.0d);
		graph.getSerie().appendData(new GraphViewData(graph.getxValue(), dato),
				true, 200);// scroll to end, true
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_recording);

		extras = getIntent().getExtras();

		currentConfiguration = (Configuration) extras
				.getSerializable("configSelected");
		recordingName = extras.getString("recordingName").toString();

		if (isServiceRunning()) {
			bindToService();
			ui_startStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
			ui_startStopbutton.setText("stop recording");
			isReceivingData = true;
			Log.d("test", "notificacionIniciaServicio");
		}

		initUI();

		ui_recordingName.setText(recordingName);
		ui_configurationName.setText(currentConfiguration.getName());
		ui_freq.setText(String.valueOf(currentConfiguration.getFrequency()) + " Hz");
		ui_bits.setText(String.valueOf(currentConfiguration.getNumberOfBits())
				+ " bits");
		ui_macAddress.setText(currentConfiguration.getMacAddress());

		String strAC = "";
		String[] ac = currentConfiguration.getActiveChannels();
		for (int i = 0; i < ac.length; i++) {
			if (ac[i].compareToIgnoreCase("null") != 0)
				strAC += "\t" + "channel " + (i + 1) + " with sensor " + ac[i]
						+ "\n";
		}

		ui_activeChannels.setText(strAC);
		graph = new HRGraph(this);
		ui_graph.addView(graph.getGraphView());

	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putAll(extras);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentConfiguration = (Configuration) savedInstanceState
				.getSerializable("configSelected");
		recordingName = savedInstanceState.getString("recordingName")
				.toString();
	}

	private void initUI() {
		ui_graph = (LinearLayout) findViewById(R.id.nr_graph_data);
		ui_startStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
		ui_recordingName = (TextView) findViewById(R.id.nr_txt_recordingName);
		ui_configurationName = (TextView) findViewById(R.id.nr_txt_configName);
		ui_bits = (TextView) findViewById(R.id.nr_txt_config_nbits);
		ui_freq = (TextView) findViewById(R.id.nr_txt_config_freq);
		ui_activeChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
		ui_macAddress = (TextView) findViewById(R.id.nr_txt_mac);
	}

	private void start_receiving_data() {
		if (isServiceBounded) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							LocalService.MSG_START_SENDING_DATA, 0, 0);
					msg.replyTo = mActivity;
					mService.send(msg);

				} catch (RemoteException e) {
				}
			}
		}
	}

	private void stop_service() {
		if (isServiceBounded) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							LocalService.MSG_STOP_SERVICE);
					msg.replyTo = mActivity;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	public void onClickedStartStop(View view) {
		if (!isServiceRunning()) {
			startService(new Intent(NewRecordingActivity.this,
					LocalService.class));
			bindToService();
			displayToast("recording started");
			ui_startStopbutton.setText("stop recording");
			isReceivingData = true;
		} else {
			unbindOfService();
			stopService(new Intent(NewRecordingActivity.this,
					LocalService.class));
			displayToast("recording stopped");
			ui_startStopbutton.setText("start recording");
			isReceivingData = false;
			saveRecording();
		}

	}

	public void saveRecording() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		Recording recording = new Recording();
		recording.setName(recordingName);
		recording.setConfig(currentConfiguration);
		recording.setSavedDate(dateFormat.format(date));
		recording.setDuration(20);
		try {
			Dao<Recording, Integer> dao = getHelper().getRecordingDao();
			dao.create(recording);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (LocalService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void displayToast(String messageToDisplay) {
		Toast t = Toast.makeText(getApplicationContext(), messageToDisplay,
				Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		t.show();

	}

	void bindToService() {
		Intent intent = new Intent(this, LocalService.class);
		intent.putExtra("recordingName", recordingName);
		intent.putExtra("configSelected", currentConfiguration);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		isServiceBounded = true;
	}

	void unbindOfService() {
		if (isServiceBounded) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							LocalService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mActivity;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			isServiceBounded = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	/*
	 * @Override protected void onSaveInstanceState(Bundle outState) {
	 * super.onSaveInstanceState(outState); outState.putString("recording_name",
	 * ui_recName.getText().toString()); //outState.putString("textIntValue",
	 * textIntValue.getText().toString()); //outState.putString("textStrValue",
	 * textStrValue.getText().toString()); }
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindOfService();
		} catch (Throwable t) {
			Log.d("bplux_service", "Failed to unbind from the service", t);
		}
	}

}
