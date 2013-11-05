package ceu.marten.activities;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import plux.android.bioplux.BPException;
import plux.android.bioplux.Device;
import plux.android.bioplux.Device.Frame;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;
import ceu.marten.data.Recording;
import ceu.marten.dataBase.DatabaseHelper;
import ceu.marten.graph.HRGraph;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.jjoe64.graphview.GraphView.GraphViewData;

public class NewRecordingActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	private Handler graphHandler;
	private Runnable run_frames;
	private Runnable run_pause;
	private Device.Frame[] tmp_frames = null;
	private ArrayList<Device.Frame> save_frames = null;
	private HRGraph HRGraph;
	private Device connection;
	private boolean bttnOn;
	private Button bttn;
	private int channel = 5; // mejor canal para las pruebas
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_recording);

		setupStartPauseButton();
		run_pause = new Runnable() {
			public void run() {
				executePauseGraphThread();
			}

			private void executePauseGraphThread() {
				for(int i =0; i<30; i++){
					HRGraph.setxValue(HRGraph.getxValue() + 1.0d);
					HRGraph.getSerie().appendData(
						new GraphViewData(HRGraph.getxValue(), 2020),
						true, 200);// scroll to end, true
				}
				graphHandler.postDelayed(run_frames, 1000);
				
			}
		};
		save_frames = new ArrayList<Device.Frame>();
		
		TextView tv =(TextView)findViewById(R.id.nr_recordingName);
		tv.setText(getIntent().getStringExtra("recordingName").toString());

	}

	private void setupStartPauseButton() {
		bttn = (Button) findViewById(R.id.ns_bttn_StartPause);
		bttn.setText("Start");
		bttnOn = false;
	}

	private void executeGraphThread() {
		
		getFrames(10);
		
		for(Frame f :tmp_frames){
			save_frames.add(f);
			HRGraph.setxValue(HRGraph.getxValue() + 1.0d);
			HRGraph.getSerie().appendData(
				new GraphViewData(HRGraph.getxValue(), f.an_in[channel]),
				true, 200);// scroll to end, true
		}
		graphHandler.postDelayed(run_pause, 1000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		graphHandler.removeCallbacks(run_frames);
	}

	public void onClickedStartPause(View view) {

		if (bttnOn) {
			graphHandler.removeCallbacks(run_frames);
			bttn.setText("start");
			bttnOn = false;
		} else {
			run_frames.run();
			bttn.setText("stop");
			bttnOn = true;
			
			
		}

	}

	private void connectTestDevice() {
		tmp_frames = new Device.Frame[10];

		// initialize frames array
		for (int i = 0; i < tmp_frames.length; i++) {
			tmp_frames[i] = new Device.Frame();
		}

		// bioPlux initialization
		try {
			connection = Device.Create("test");// Device mac addr
												// 00:07:80:4C:2A:FB
			connection.BeginAcq();
		} catch (BPException e) {
			e.printStackTrace();
		}

		graphHandler = new Handler();
		run_frames = null;
		HRGraph = new HRGraph(this);

		run_frames = new Runnable() {
			public void run() {
				executeGraphThread();
			}
		};

		LinearLayout data_ly = (LinearLayout) findViewById(R.id.ns_data);
		data_ly.addView(HRGraph.getGraphView());
	}

	private void getFrames(int nFrames) {
		try {
			connection.GetFrames(nFrames, tmp_frames);
		} catch (BPException e) {
			e.printStackTrace();
		}

	}

	public void onClickedConnect(View view) {
		connectTestDevice();
	}
	
	public void onClickedSave(View view)  {
		Recording newRecording = new Recording();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		
		newRecording.setDuration(20);
		Configuration config = new Configuration();
		newRecording.setConfig(config);
		newRecording.setStartDate(dateFormat.format(date));
		
		TextView tv =(TextView)findViewById(R.id.nr_recordingName);
		newRecording.setName(tv.getText().toString());
		
		saveRecording(newRecording);
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("session", newRecording);
		setResult(RESULT_OK, returnIntent);
		finish();

		Toast t;
		t = Toast.makeText(getApplicationContext(),
				"session successfully created", Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		t.show();
	}
	
	public void saveRecording(Recording recording) {
		try {
			Dao<Recording, Integer> dao = getHelper().getSessionDao();
			dao.create(recording);
	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}

}
