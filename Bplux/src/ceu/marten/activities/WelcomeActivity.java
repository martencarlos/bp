package ceu.marten.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ceu.marten.bplux.R;

public class WelcomeActivity extends Activity {

	Intent settingsIntent;
	Intent graphIntent;
	
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(
				@SuppressWarnings("rawtypes") AdapterView parent, View v,
				int position, long id) {
			switch (position) {
			case 0:
				startActivity(settingsIntent);
				
				break;
			case 1:
				startActivity(graphIntent);
				break;
			}
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);

		settingsIntent = new Intent(WelcomeActivity.this,SettingsActivity.class);
		graphIntent = new Intent(WelcomeActivity.this, GraphActivity.class);
		
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(mMessageClickedHandler);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}

	public void launchSettings(View v) {
		Intent intent = new Intent(WelcomeActivity.this, SettingsActivity.class);
		startActivity(intent);
	}

	public void launchGraph(View v) {
		Intent intent = new Intent(WelcomeActivity.this, GraphActivity.class);
		startActivity(intent);
	}

}
