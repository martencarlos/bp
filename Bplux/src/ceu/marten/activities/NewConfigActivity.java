package ceu.marten.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ceu.marten.adapters.ActiveChannelsListAdapter;
import ceu.marten.adapters.ChannelsToDisplayListAdapter;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;

public class NewConfigActivity extends Activity {

	int freq=500;
	int nbits=8;
	Configuration config;
	String[] channelsActivated = null; // the ones that are not null
	boolean[] channelsToDisplay= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_config);

		/* initialize view components */
		channelsToDisplay= new boolean[8];
		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					((TextView) findViewById(R.id.freq_view)).setText(String
							.valueOf(progress + 36));
					freq = progress;
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		final EditText freqView = (EditText) findViewById(R.id.freq_view);
		freqView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
					int new_frec = Integer.parseInt(v.getText().toString());
					if (new_frec > 36 && new_frec <= 1000) {
						frequency.setProgress((new_frec - 36));
						freq = new_frec;
					} else if(new_frec > 1000){
						frequency.setProgress(1000);
						freqView.setText("1000");
						freq = 1000;
						displayToast("max freq is 1000 Hz");
					}else {
						frequency.setProgress(0);
						freqView.setText("36");
						freq = 36;
						displayToast("min freq is 36 Hz");
					}
					freqView.clearFocus();
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					handled = true;
				}
				return handled;

			}
		});

		/* initialize variables */
		config = new Configuration();

	}

	private void setupActiveChannelsDialog() {

		final ArrayList<String> channels = new ArrayList<String>();
		channels.add("channel 1");
		channels.add("channel 2");
		channels.add("channel 3");
		channels.add("channel 4");
		channels.add("channel 5");
		channels.add("channel 6");
		channels.add("channel 7");
		channels.add("channel 8");
		final ActiveChannelsListAdapter acla = new ActiveChannelsListAdapter(
				this, channels);
		AlertDialog.Builder activeChannelsBuilder;
		AlertDialog activeChannelsDialog;
		ListView lv;

		// ACTIVE CHANNELS BUILDER
		activeChannelsBuilder = new AlertDialog.Builder(this);
		activeChannelsBuilder.setIcon(R.drawable.ic_launcher);
		activeChannelsBuilder.setTitle("Select active channels");
		activeChannelsBuilder.setView(getLayoutInflater().inflate(
				R.layout.dialog_channels_listview, null));

		activeChannelsBuilder.setPositiveButton("accept",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TextView ca = (TextView) findViewById(R.id.txt_canales_activos);
						channelsActivated = acla.getChecked();
						int counter=0;
						for (int i = 0; i < channelsActivated.length; i++) {
							if (channelsActivated[i] != null)
								counter++;
						}
						if (counter == 0)
							ca.setText("no channels selected");
						else {
							ca.setText("channels to activate: ");

							String si = "";
							for (int i = 0; i < channelsActivated.length; i++) {
								if (channelsActivated[i] != null)
									si = si + "\n\t channel " + (i + 1)
											+ " with sensor "
											+ channelsActivated[i];
							}
							ca.append(si);
						}
					}
				});
		activeChannelsBuilder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		activeChannelsDialog = activeChannelsBuilder.create();
		activeChannelsDialog.show();

		lv = (ListView) activeChannelsDialog
				.findViewById(R.id.lv_channelsSelection);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.setItemsCanFocus(false);
		lv.setAdapter(acla);

	}

	private void setupChannelsToDisplay() {

		final ArrayList<String> channels = new ArrayList<String>();
		final ArrayList<String> sensors = new ArrayList<String>();
		for (int i = 0; i < channelsActivated.length; i++) {
			if (channelsActivated[i] != null) {
				channels.add("channel " + (i + 1));
				sensors.add(channelsActivated[i]);
			}
		}

		final ChannelsToDisplayListAdapter ctdla = new ChannelsToDisplayListAdapter(
				this, channels, sensors);
		AlertDialog.Builder channelsToDisplayBuilder;
		AlertDialog channelsToDisplayDialog;
		ListView lv;

		// CHANNELS TO DISPLAY BUILDER
		channelsToDisplayBuilder = new AlertDialog.Builder(this);
		channelsToDisplayBuilder.setIcon(R.drawable.ic_launcher);
		channelsToDisplayBuilder.setTitle("Select channels to display");
		channelsToDisplayBuilder.setView(getLayoutInflater().inflate(
				R.layout.dialog_channels_listview, null));

		channelsToDisplayBuilder.setPositiveButton("accept",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean[] channelsSelected = ctdla.getChecked();
						TextView ca = (TextView) findViewById(R.id.nc_txt_channels_to_show);

						int counter = 0;
						for (int i = 0; i < channelsSelected.length; i++) {
							if (channelsSelected[i]) {
								counter++;
							}
						}
						if (counter > 3)
							displayToast("channels to display have to be less than 4");
						else if (counter == 0)
							ca.setText("no channels selected");
						else {
							ca = (TextView) findViewById(R.id.nc_txt_channels_to_show);
							ca.setText("channels to show: ");
							String si = "";
							for (int i = 0; i < channelsSelected.length; i++) {
								if (channelsSelected[i]) {
									Log.d("test", "channel number: "+ channels.get(i).toString().charAt(channels.get(i).toString().length()-1));
									int in=Character.getNumericValue((channels.get(i).toString().charAt(channels.get(i).toString().length()-1)));
									channelsToDisplay[in] = true;
									
									si = si + "\n\t"
											+ channels.get(i).toString()
											+ " with sensor "
											+ sensors.get(i).toString();
								}
							}
							ca.append(si);
						}

					}
				});
		channelsToDisplayBuilder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		channelsToDisplayDialog = channelsToDisplayBuilder.create();
		channelsToDisplayDialog.show();

		lv = (ListView) channelsToDisplayDialog
				.findViewById(R.id.lv_channelsSelection);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.setItemsCanFocus(false);
		lv.setAdapter(ctdla);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	private void displayToast(String messageToDisplay) {
		Toast t = Toast.makeText(getApplicationContext(), messageToDisplay,
				Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		t.show();
	}

	public void onClickedSubmit(View view) {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		config.setName(((EditText) findViewById(R.id.dev_name)).getText()
				.toString());
		config.setMac_address(((EditText) findViewById(R.id.nc_mac_address)).getText()
				.toString());
		config.setFreq(freq);
		config.setnBits(nbits);
		config.setCreateDate(dateFormat.format(date));
		config.setActiveChannels(channelsActivated);
		config.setchannelsToDisplay(channelsToDisplay);
		Intent returnIntent = new Intent();
		returnIntent.putExtra("config", config);
		setResult(RESULT_OK, returnIntent);
		finish();

		Toast t;
		t = Toast.makeText(getApplicationContext(),
				"config successfully created", Toast.LENGTH_SHORT);
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

	public void onClickedChannelPickDialog(View view) {
		setupActiveChannelsDialog();
	}

	public void onClickedChannelDisplayPickDialog(View view) {
		
		if(channelsActivated != null){
			int counter = 0;
			for (int i = 0; i < channelsActivated.length; i++) {
				if (channelsActivated[i]!=null) {
					counter++;
				}
			}
			if (counter != 0)
				setupChannelsToDisplay();
			else
				displayToast("please select active channels first");
		}
		else
			displayToast("please select active channels first");
	}
}
