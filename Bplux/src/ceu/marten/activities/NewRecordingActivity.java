package ceu.marten.activities;

import android.app.Activity;
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
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;
import ceu.marten.graph.HRGraph;
import ceu.marten.services.LocalService;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class NewRecordingActivity extends Activity {

	private LinearLayout ui_graph;
	private TextView ui_recName, ui_configName, ui_bits, ui_freq, ui_aChannels, ui_macAddr;
	private Button ui_startStop;
	private Chronometer duration;

	private Configuration currentConfig;

	Messenger mService = null;
	static int dato = 0;
	private boolean isServiceStarted=false;
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
			/*
			 * case LocalService.MSG_SET_STRING_VALUE: String str1 =
			 * msg.getData().getString("str1");
			 * Log.d("bplux_service","Int Message: " + str1); break;
			 */
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
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
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
		
		Bundle extras = getIntent().getExtras();
		
		if(extras.getBoolean("notification")!=false){
		    Log.d("test", "comes from a notification with value: "+extras.getBoolean("notification"));
		}else{
			
		
		initUI();
		currentConfig = (Configuration) extras.getSerializable(
				"configSelected");
		ui_recName.setText(extras.getString("recordingName")
				.toString());
		ui_configName.setText(currentConfig.getName());
		ui_freq.setText(String.valueOf(currentConfig.getFreq())+" Hz");
		ui_bits.setText(String.valueOf(currentConfig.getnBits())+" bits");
		ui_macAddr.setText(currentConfig.getMac_address());
		
		String strAC="";
		String[] ac = currentConfig.getActiveChannels();
		for(int i=0;i<ac.length;i++){
			if(ac[i].compareToIgnoreCase("null")!=0)
				strAC+="\t"+"channel "+(i+1)+" with sensor "+ac[i]+"\n";
		}
			
		ui_aChannels.setText(strAC);
		// restoreMeIfNeeded(savedInstanceState);
		//bindIfServiceRunning();
		graph = new HRGraph(this);
		ui_graph.addView(graph.getGraphView());
		}
	}

	private void initUI() {
		ui_graph = (LinearLayout) findViewById(R.id.nr_graph_data);
		ui_startStop = (Button) findViewById(R.id.nr_bttn_StartPause);
		ui_recName = (TextView) findViewById(R.id.nr_txt_recordingName);
		ui_configName = (TextView) findViewById(R.id.nr_txt_configName);
		ui_bits = (TextView) findViewById(R.id.nr_txt_config_nbits);
		ui_freq = (TextView) findViewById(R.id.nr_txt_config_freq);
		ui_aChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
		ui_macAddr = (TextView) findViewById(R.id.nr_txt_mac);
	}

	private void restoreMeIfNeeded(Bundle state) {
		if (state != null) {
			ui_recName.setText(state.getString("recording_name"));
			// textIntValue.setText(state.getString("textIntValue"));
			// textStrValue.setText(state.getString("textStrValue"));
		}
	}

	private void bindIfServiceRunning() {
		if (LocalService.isRunning()) {
			bindToService();
		}
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

	private void stop_receiving_data() {
		if (isServiceBounded) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							LocalService.MSG_STOP_SENDING_DATA, 0, 0);
					msg.replyTo = mActivity;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	public void onClickedStartStop(View view) {
		if (!isServiceStarted) {
			startService(new Intent(NewRecordingActivity.this, LocalService.class));
			bindToService();
			displayToast("recording started");
			ui_startStop.setText("stop recording");
			isReceivingData=true;
			isServiceStarted= true;
		}else{
			unbindOfService();
			displayToast("recording stopped");
			ui_startStop.setText("start recording");
			isReceivingData=false;
			isServiceStarted= false;
		}
			
	}
	
	private void displayToast(String messageToDisplay) {
		Toast t = Toast.makeText(getApplicationContext(), messageToDisplay,
				Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		t.show();

	}

	void bindToService() {
		Intent intent = new Intent(this, LocalService.class);
		intent.putExtra("recName",
				ui_configName.getText().toString());
		intent.putExtra("currentConfig", currentConfig);
		bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);
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
			Log.d("bplux_service", "unbinding!");
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
