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
import ceu.marten.bplux.R;
import ceu.marten.graph.HRGraph;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class GraphActivity extends Activity {

	private Handler graphHandler;
	private Runnable runnable;
	private HRGraph HRGraph;
	private BPDevice device;
	private boolean bttnOn;
	private Button bttn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_layout);
		
		setupPhysicalGraph();
		setupButtons();
		setupDetails();
	}
	
	
	private void setupPhysicalGraph(){
		graphHandler = new Handler();
		runnable = null;
		HRGraph = new HRGraph(this);
		device = new BPDevice("test");
		
		runnable = new Runnable() {
			public void run() {
				executeGraphThread();
			}
		};
		device.beginAcq();
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(HRGraph.getGraphView());
	}

	private void setupButtons(){
		bttn = (Button) findViewById(R.id.BttnStartStop);
		bttn.setText("Start");
		bttnOn = false;
	}
	
	private void setupDetails(){
		BPDevice device = ((BPDevice) getIntent()
				.getSerializableExtra("connectedDevice"));
		((TextView) findViewById(R.id.gl_currentConnectedDevice)).setText("current connected device: "+device
				.getName());
	}
	
	private void executeGraphThread() {
		HRGraph.setxValue(HRGraph.getxValue() + 1.0d);
		HRGraph.getSerie().appendData(
				new GraphViewData(HRGraph.getxValue(), device.getFrame(1)),
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

	public void onClickedStartStopGraph(View view) {

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
