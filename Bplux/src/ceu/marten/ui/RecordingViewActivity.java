package ceu.marten.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import ceu.marten.bplux.R;

public class RecordingViewActivity extends Activity {

	private static final String TAG = RecordingViewActivity.class.getName();
	private String recording_name = "";
	private TextView ui_recording_data = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_recording_view);

		Bundle b = getIntent().getExtras();
		recording_name = b.getString("recordingName");
		findViews();
		 new ReadFile().execute(""); //read async mode
	}

	private void findViews() {
		ui_recording_data = (TextView) findViewById(R.id.rv_txt_recording);
	}

	// BUTTON EVENTS
	public void sendDataViaEmail(View v) {
		File F = new File(recording_name + ".txt");
		Uri U = Uri.fromFile(F);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/rtf");
		i.putExtra(Intent.EXTRA_STREAM, U);
		startActivity(Intent.createChooser(i, "select email client"));
	}

	private class ReadFile extends AsyncTask<String, Void, String> {

		StringBuilder buf;
		@Override
		protected String doInBackground(String... params) {
			InputStream in = null;
			try {
				in = openFileInput(recording_name + ".txt");
				if (in != null) {
					InputStreamReader tmp = new InputStreamReader(in);
					BufferedReader reader = new BufferedReader(tmp);
					String str;
					buf = new StringBuilder();
					while ((str = reader.readLine()) != null) {
						buf.append(str + "\n");
					}
					in.close();
				}
			} catch (FileNotFoundException e) {
				Log.e(TAG, "file to read not found", e);
			} catch (IOException e) {
				Log.e(TAG, "inputStream/bufferedReader exception, reading file", e);
			}
			return "execuded";
		}

		@Override
		protected void onPostExecute(String result) {
			ui_recording_data.setTypeface(Typeface.MONOSPACE);
			ui_recording_data.setText(buf.toString());
		}

		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}
