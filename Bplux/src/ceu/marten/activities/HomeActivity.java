package ceu.marten.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/* BUTTON EVENTS */
	public void onClickedConfigs(View view) {
		Intent intent = new Intent(this, RecordingConfigsActivity.class);
		startActivity(intent);
	}

	public void onClickedSessions(View view) {
		Intent intent = new Intent(this, StoredRecordingsActivity.class);
		startActivity(intent);
	}
}
