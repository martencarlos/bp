package ceu.marten.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import ceu.marten.bplux.R;
import ceu.marten.ui.dialogs.AboutDialog;
import ceu.marten.ui.dialogs.HelpDialog;

public class HomeActivity extends Activity implements android.widget.PopupMenu.OnMenuItemClickListener {

	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.gm_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	            return true;
	            
	        case R.id.gm_help:
	        	HelpDialog help = new HelpDialog(this);
	        	help.setTitle(getString(R.string.gm_help_title));
	        	help.setCanceledOnTouchOutside(true);
	        	help.show();
	        	return true;
	        	
	        case R.id.gm_about:
	        	AboutDialog about = new AboutDialog(this);
	        	about.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        	about.setCanceledOnTouchOutside(true);
	        	about.show();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ly_home);
	}

	/************************ BUTTON EVENTS *******************/
	
	public void onClikedMenuItems(View v) {
	    PopupMenu popup = new PopupMenu(this, v);
	    popup.setOnMenuItemClickListener(this);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.global_menu, popup.getMenu());
	    popup.show();
	}
	
	public void onClickedStartRecording(View view) {
		startActivity(new Intent(this, ConfigurationsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

	}

	public void onClickedBrowseRecordings(View view) {
		startActivity(new Intent(this, RecordingsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
