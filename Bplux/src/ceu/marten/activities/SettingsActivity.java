package ceu.marten.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import ceu.marten.bplux.R;

public class SettingsActivity extends Activity implements OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);

		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView) findViewById(R.id.freq_view)).setText(String
						.valueOf(progress + " Hz"));
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		
		Spinner spinner = (Spinner) findViewById(R.id.dev_channel);
		spinner.setOnItemSelectedListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	   
	    switch(view.getId()) {
	        case R.id.radioBttn8:
	            if (checked)
	                // change bits on Device to 8
	            break;
	        case R.id.radioBttn12:
	            if (checked)
	            	// change bits on Device to 12
	            break;
	    }
	}
	
	public void submitSettings(View view){
		Toast.makeText(getApplicationContext(), "settings saved", Toast.LENGTH_SHORT).show();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}
