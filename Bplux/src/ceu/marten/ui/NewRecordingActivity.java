package ceu.marten.ui;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
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
			uiReceptionFrequency, uiSamplingFrequency, uiActiveChannels,
			uiMacAddress;
	private Button uiStartStopbutton;
	private static Chronometer chronometer;
	private boolean isChronometerRunning;

	private static Configuration currentConfiguration;
	private String recordingName;
	private String duration;
	private Bundle extras;

	private Messenger mService = null;
	private static HRGraph[] graphs;
	private static double lastXValue;
	private static double period;
	private boolean isServiceBounded;
	private Context context = this;
	private AlertDialog dialog;
	private LayoutInflater inflater;
	private final Messenger mActivity = new Messenger(new IncomingHandler());

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BiopluxService.MSG_DATA:
				appendDataToGraphs(msg.getData().getShortArray("frame"));
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

	static void appendDataToGraphs(short[] data) {
		lastXValue++;
		for(int i=0;i< graphs.length;i++)
			graphs[i].getSerie().appendData(new GraphViewData(
					(period * lastXValue),
						data[currentConfiguration.getChannelsToDisplay().get(i)-1]), true, 600);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_recording);

		// GETTING EXTRA INFO FROM INTENT
		extras = getIntent().getExtras();
		currentConfiguration = (Configuration) extras
				.getSerializable("configSelected");
		recordingName = extras.getString("recordingName").toString();
		
		// INIT VARIABLES
		int numberOfChannelsToDisplay = currentConfiguration.getNumberOfChannelsToDisplay();
		inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		graphs = new HRGraph[numberOfChannelsToDisplay];
		int samplingFrames = currentConfiguration.getReceptionFrequency()/currentConfiguration.getSamplingFrequency();
		period = samplingFrames*1000d / currentConfiguration.getReceptionFrequency();
		isChronometerRunning = false;
		isServiceBounded = false;
		lastXValue = 0;

		// INIT LAYOUT
		
		LayoutParams graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				450);
		LayoutParams detailParameters = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		/*LayoutParams buttonParameters = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);*/
		View graphsView = findViewById(R.id.nr_graphs);

		// ON SCREEN ROTATION, RESTORE GRAPH VIEWS
		@SuppressWarnings("deprecation")
		final Object data = getLastNonConfigurationInstance();
		if (data != null) {
			graphs = (HRGraph[]) data;
			for (int i = 0; i < graphs.length; i++) {
				((ViewGroup) (graphs[i].getGraphView().getParent())).removeView(graphs[i].getGraphView());
				View graph = inflater.inflate(R.layout.in_ly_graph, null);
				((ViewGroup) graph).addView(graphs[i].getGraphView());
				((ViewGroup) graphsView).addView(graph, graphParams);
			}
		} else {
			for (int i = 0; i < numberOfChannelsToDisplay; i++) {
				graphs[i] = new HRGraph(this,getString(R.string.nc_dialog_channel)+ " "+ currentConfiguration.getChannelsToDisplay().get(i).toString());
				View graph = inflater.inflate(R.layout.in_ly_graph, null);
				((ViewGroup) graph).addView(graphs[i].getGraphView());
				((ViewGroup) graphsView).addView(graph, graphParams);
			}
		}
		// FIND ACTIVITY GENERAL VIEWS
		uiRecordingName = (TextView) findViewById(R.id.nr_txt_recordingName);
		uiRecordingName.setText(recordingName);
		uiStartStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
		chronometer = (Chronometer) findViewById(R.id.nr_chronometer);

		if (numberOfChannelsToDisplay == 1) {
			View details = inflater.inflate(R.layout.in_ly_graph_details, null);
			((ViewGroup) graphsView).addView(details, detailParameters);
			findDetailViews();

			uiConfigurationName.setText(currentConfiguration.getName());
			uiReceptionFrequency.setText(String.valueOf(currentConfiguration
					.getReceptionFrequency()) + " Hz");
			uiSamplingFrequency.setText(String.valueOf(currentConfiguration
					.getSamplingFrequency()) + " Hz");
			uiNumberOfBits.setText(String.valueOf(currentConfiguration
					.getNumberOfBits()) + " bits");
			uiMacAddress.setText(currentConfiguration.getMacAddress());
			uiActiveChannels.setText(currentConfiguration
					.getActiveChannelsAsString());
		}

		/*View buttons = inflater.inflate(R.layout.in_ly_buttons, null);
		((ViewGroup) graphsView).addView(buttons, buttonParameters);
		 */
		// IF SERVICE WAS RUNNING BIND TO IT
		if (isServiceRunning()) {
			bindToService();
			uiStartStopbutton.setText(getString(R.string.nr_button_stop));
		}

		setupBackDialog();

	}

	private void setupBackDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView customTitleView = (TextView)inflater.inflate(R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.nr_back_dialog_title);
		builder.setMessage(getString(R.string.nr_back_dialog_message)).setCustomTitle(customTitleView);

		builder.setPositiveButton(getString(R.string.nr_dialog_positive_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new saveRecording().execute("");
						Intent backIntent = new Intent(context,
								ConfigurationsActivity.class);
						startActivity(backIntent);
						overridePendingTransition(R.anim.slide_in_left,
								R.anim.slide_out_right);
						finish();
					}
				});
		builder.setNegativeButton(getString(R.string.nc_dialog_negative_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// dialog gets closed
					}
				});

		dialog = builder.create();

	}

	@Override
	public void onBackPressed() {
		if (isChronometerRunning)
			dialog.show();
		else {
			Intent backIntent = new Intent(context,
					ConfigurationsActivity.class);
			startActivity(backIntent);
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			super.onBackPressed();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (isChronometerRunning) {
			extras.putLong("chronometerBase", chronometer.getBase());
			extras.putDouble("xcounter", lastXValue);
		} else
			extras.putLong("chronometerBase", 0);

		savedInstanceState.putAll(extras);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return graphs;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.getLong("chronometerBase") != 0) {
			chronometer.setBase(savedInstanceState.getLong("chronometerBase"));
			chronometer.start();
			isChronometerRunning = true;
			lastXValue = savedInstanceState.getDouble("xcounter");
		}

		currentConfiguration = (Configuration) savedInstanceState
				.getSerializable("configSelected");
		recordingName = savedInstanceState.getString("recordingName")
				.toString();
	}

	private void findDetailViews() {
		uiConfigurationName = (TextView) findViewById(R.id.nr_txt_configName);
		uiNumberOfBits = (TextView) findViewById(R.id.nr_txt_config_nbits);
		uiReceptionFrequency = (TextView) findViewById(R.id.nr_reception_freq);
		uiSamplingFrequency = (TextView) findViewById(R.id.nr_sampling_freq);
		uiActiveChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
		uiMacAddress = (TextView) findViewById(R.id.nr_txt_mac);
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
					Log.e(TAG, "Error sending duration to service", e);
				}
			}
		}
	}

	public void onClickedStartStop(View view) {
		if (!isServiceRunning()) {
			startService(new Intent(NewRecordingActivity.this,
					BiopluxService.class));
			bindToService();
			displayInfoToast(getString(R.string.nr_info_started));
			uiStartStopbutton.setText(getString(R.string.nr_button_stop));
			startChronometer();

		} else {
			new saveRecording().execute("");
			uiStartStopbutton.setText(getString(R.string.nr_button_start));
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
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.UK);
		duration = formatter.format(elapsedMiliseconds);
		isChronometerRunning = false;
	}

	public void saveRecording() {
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
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

	private class saveRecording extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			stopChronometer();
			sendRecordingDuration();
			saveRecording();
			unbindOfService();
			stopService(new Intent(NewRecordingActivity.this,
					BiopluxService.class));
			return "executed";
		}

		@Override
		protected void onPostExecute(String result) {
			displayInfoToast(getString(R.string.nr_info_rec_saved));
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
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
	protected void onDestroy() {
		Log.d(TAG, "destroy");
		super.onDestroy();
		try {
			unbindOfService();
		} catch (Throwable t) {
			Log.e(TAG,
					"failed to unbind from service when activity is destroyed",
					t);
		}
	}

}
