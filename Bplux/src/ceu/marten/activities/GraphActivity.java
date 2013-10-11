package ceu.marten.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.HRGraph;
import ceu.marten.bplux.HRSimulator;
import ceu.marten.bplux.R;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class GraphActivity extends Activity {

	private Handler graphHandler;
	private Runnable runnable;
	private HRGraph HRGraph;
	private HRSimulator hrsim;
	private boolean bttnOn;
	private Button bttn;
	private BPDevice device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_layout);
		
		device = new BPDevice();
		device = ((BPDevice) getIntent()
				.getSerializableExtra("connectedDevice"));
		((TextView) findViewById(R.id.connected_device_name)).setText(device.getName());
		
		
		bttn = (Button) findViewById(R.id.BttnStartStop);
		bttn.setText("Start");
		bttnOn = false;
		graphHandler = new Handler();
		runnable = null;
		HRGraph = new HRGraph(this);
		hrsim = new HRSimulator();
		runnable = new Runnable() {
			public void run() {
				executeGraphThread();
			}
		};

		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(HRGraph.getGraphView());
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void executeGraphThread() {
		HRGraph.setxValue(HRGraph.getxValue() + 1.0d);
		HRGraph.getSerie().appendData(
				new GraphViewData(HRGraph.getxValue(), hrsim.getAdultsBPM()),
				true, 70);// scroll to end, true
		graphHandler.postDelayed(runnable, 100);
	}

	@Override
	protected void onPause() {
		super.onPause();
		graphHandler.removeCallbacks(runnable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.graph_menu, menu);
		return true;
	}

	public void startStopGraph(View view) {

		if (bttnOn) {
			graphHandler.removeCallbacks(runnable);
			bttn.setText("start");
			bttnOn = false;
		} else {
			runnable.run();
			bttn.setText("stop");
			bttnOn = true;
		}

	}

}
