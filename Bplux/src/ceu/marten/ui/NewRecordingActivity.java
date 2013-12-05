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
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.model.Recording;
import ceu.marten.model.io.DatabaseHelper;
import ceu.marten.services.BiopluxService;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.jjoe64.graphview.GraphView.GraphViewData;

public class NewRecordingActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	private static final String TAG = NewRecordingActivity.class.getName();

	private TextView uiRecordingName, uiConfigurationName, uiNumberOfBits,
			uiFrequency, uiActiveChannels, uiMacAddress;
	private LinearLayout uiGraph;
	private Button uiStartStopbutton;
	private Chronometer chronometer;
	private boolean isChronometerRunning = false;

	private Configuration currentConfiguration;
	private String recordingName;
	String duration;
	private Bundle extras;

	private Messenger mService = null;
	private static int dato = 0;
	private static HRGraph graph;
	private boolean isServiceBounded = false;
	private final Messenger mActivity = new Messenger(new IncomingHandler());

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BiopluxService.MSG_VALUE:
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
						BiopluxService.MSG_REGISTER_CLIENT);
				msg.replyTo = mActivity;
				mService.send(msg);
			} catch (RemoteException e) {
				Log.e(TAG, "service conection failed", e);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Log.i(TAG, "service disconnected");
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
		findViews();
		
		extras = getIntent().getExtras();
		currentConfiguration = (Configuration) extras
				.getSerializable("configSelected");
		recordingName = extras.getString("recordingName").toString();

		if (isServiceRunning()) {
			bindToService();
			uiStartStopbutton.setText("stop recording");
		}

		uiRecordingName.setText(recordingName);
		uiConfigurationName.setText(currentConfiguration.getName());
		uiFrequency.setText(String.valueOf(currentConfiguration.getFrequency())
				+ " Hz");
		uiNumberOfBits.setText(String.valueOf(currentConfiguration
				.getNumberOfBits()) + " bits");
		uiMacAddress.setText(currentConfiguration.getMacAddress());
		uiActiveChannels.setText(currentConfiguration
				.getActiveChannelsAsString());

		graph = new HRGraph(this);
		uiGraph.addView(graph.getGraphView());

	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		if (isChronometerRunning)
			extras.putLong("chronometerBase", chronometer.getBase());
		else
			extras.putLong("chronometerBase", 0);
		Log.d(TAG, "guardo instancia");
		savedInstanceState.putAll(extras);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.getLong("chronometerBase") != 0) {
			chronometer.setBase(savedInstanceState.getLong("chronometerBase"));
			chronometer.start();
			isChronometerRunning = true;
		}
		currentConfiguration = (Configuration) savedInstanceState
				.getSerializable("configSelected");
		recordingName = savedInstanceState.getString("recordingName")
				.toString();

		super.onRestoreInstanceState(savedInstanceState);
	}

	private void findViews() {
		uiGraph = (LinearLayout) findViewById(R.id.nr_graph_data);
		uiStartStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
		uiRecordingName = (TextView) findViewById(R.id.nr_txt_recordingName);
		uiConfigurationName = (TextView) findViewById(R.id.nr_txt_configName);
		uiNumberOfBits = (TextView) findViewById(R.id.nr_txt_config_nbits);
		uiFrequency = (TextView) findViewById(R.id.nr_txt_config_freq);
		uiActiveChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
		uiMacAddress = (TextView) findViewById(R.id.nr_txt_mac);
		chronometer = (Chronometer) findViewById(R.id.nr_chronometer);
	}

	private void sendRecordingDuration() {
		if (isServiceBounded) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							BiopluxService.MSG_RECORDING_DURATION, 0, 0);
					Bundle extras = new Bundle();
					extras.putString("duration", duration);
					msg.setData(extras);
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
					BiopluxService.class));
			bindToService();
			displayInfoToast("recording started");
			uiStartStopbutton.setText("stop recording");
			startChronometer();

		} else {
			stopChronometer();
			sendRecordingDuration();
			unbindOfService();
			stopService(new Intent(NewRecordingActivity.this,
					BiopluxService.class));
			displayInfoToast("recording stopped");
			uiStartStopbutton.setText("start recording");
			saveRecording();
		}

	}
	private void displayInfoToast(String messageToDisplay) {
		Toast infoToast = new Toast(getApplicationContext());

		LayoutInflater inflater = getLayoutInflater();
		View toastView = inflater.inflate(R.layout.toast_info, null);
		infoToast.setView(toastView);
		((TextView) toastView.findViewById(R.id.display_text))
				.setText(messageToDisplay);

		infoToast.show();
	}

	private void startChronometer() {
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
		isChronometerRunning = true;
	}

	private void stopChronometer() {
		chronometer.stop();
		Date elapsedMiliseconds = new Date(SystemClock.elapsedRealtime()
				- chronometer.getBase());
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		duration = formatter.format(elapsedMiliseconds);
		isChronometerRunning = false;
	}

	public void saveRecording() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		Recording recording = new Recording();
		recording.setName(recordingName);
		recording.setConfig(currentConfiguration);
		recording.setSavedDate(dateFormat.format(date));
		recording.setDuration(duration);
		try {
			Dao<Recording, Integer> dao = getHelper().getRecordingDao();
			dao.create(recording);
		} catch (SQLException e) {
			Log.e(TAG, "saving recording exception", e);
		}

	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (BiopluxService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	void bindToService() {
		Intent intent = new Intent(this, BiopluxService.class);
		intent.putExtra("recordingName", recordingName);
		intent.putExtra("configSelected", currentConfiguration);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		isServiceBounded = true;
	}

	void unbindOfService() {
		if (isServiceBounded) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							BiopluxService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mActivity;
					mService.send(msg);
				} catch (RemoteException e) {
					Log.e(TAG, "Service crashed", e);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();	
		try {
			unbindOfService();
		} catch (Throwable t) {
			Log.e(TAG,"failed to unbind from service when activity is destroyed",t);
		}
	}

}
