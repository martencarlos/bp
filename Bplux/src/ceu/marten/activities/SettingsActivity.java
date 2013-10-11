package ceu.marten.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import ceu.marten.bplux.BPDevice;
import ceu.marten.bplux.R;

public class SettingsActivity extends Activity implements
		OnItemSelectedListener {

	BPDevice device;
	int channel = 0;
	int freq = 0;
	int nbits = 8;
	boolean digOutput = false;
	boolean test = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		/* initialize view components */
		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);

		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView) findViewById(R.id.freq_view)).setText(String
						.valueOf(progress + " Hz"));
				freq = progress;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		Spinner spinner = (Spinner) findViewById(R.id.dev_channel);
		spinner.setOnItemSelectedListener(this);

		/* initialize variables */
		device = new BPDevice();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	public void onClickedSubmit(View view) {

		device.setName(((EditText) findViewById(R.id.dev_name)).getText()
				.toString());
		device.setChannel(channel);
		device.setFreq(freq);
		device.setnBits(nbits);
		device.setDigOutput(digOutput);
		device.setisSimDevice(((ToggleButton) findViewById(R.id.tbTest))
				.isChecked());

		Intent returnIntent = new Intent();
		returnIntent.putExtra("deviceSettings", device);
		setResult(RESULT_OK, returnIntent);
		finish();

		Toast t;
		t = Toast.makeText(getApplicationContext(),
				"device successfully created", Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		t.show();
	}

	public void onClickedCancel(View view) {
		finish();
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.radioBttn8:
			if (checked)
				nbits = 8;
			break;
		case R.id.radioBttn12:
			if (checked)
				nbits = 12;
			break;
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		channel = pos + 1;
	}

	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	public void onCheckboxClicked(View view) {

		if (((CheckBox) view).isChecked())
			digOutput = true;
		else
			digOutput = false;
	}
}
