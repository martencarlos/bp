package ceu.marten.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import ceu.marten.bplux.R;

public class HomeActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.global_menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.gm_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	            return true;
	        case R.id.gm_help:
	        	Toast.makeText(this, "Help menu is under construction", Toast.LENGTH_LONG).show();
	            return true;
	        default:
	        	Toast.makeText(this, "About menu is under construction", Toast.LENGTH_LONG).show();
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_home);
	}

	/* BUTTON EVENTS */
	public void onClickedStartRecording(View view) {
		startActivity(new Intent(this, ConfigurationsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

	}

	public void onClickedBrowseRecordings(View view) {
		startActivity(new Intent(this, RecordingsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
