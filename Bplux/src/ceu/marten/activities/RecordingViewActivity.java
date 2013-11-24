package ceu.marten.activities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import ceu.marten.bplux.R;

public class RecordingViewActivity extends Activity {

	private String recording_name="";
	private TextView ui_recording_data = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_recording_view);
		Bundle b = getIntent().getExtras();
		recording_name = b.getString("recordingName");
		initUI();
		readFile();
	}
	
	private void initUI() {
		ui_recording_data = (TextView) findViewById(R.id.rv_txt_recording);
	}

	private void readFile() {
		InputStream in = null;
		try {
			in = openFileInput(recording_name+".txt");
			if (in != null) {
				InputStreamReader tmp = new InputStreamReader(in);
				BufferedReader reader = new BufferedReader(tmp);
				String str;
				StringBuilder buf = new StringBuilder();
				while ((str = reader.readLine()) != null) {
					buf.append(str + "\n");
				}
				in.close();
				ui_recording_data.setTypeface(Typeface.MONOSPACE);
				ui_recording_data.setText(buf.toString());
			}
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
}
