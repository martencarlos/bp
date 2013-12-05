package ceu.marten.ui;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import ceu.marten.bplux.R;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_home);
	}

	/* BUTTON EVENTS */
	public void onClickedStartRecording(View view) {
		Intent intent = new Intent(this, ConfigurationsActivity.class);
		startActivity(intent);

	}

	public void onClickedBrowseRecordings(View view) {
		Intent intent = new Intent(this, RecordingsActivity.class);
		startActivity(intent);
	}
}
