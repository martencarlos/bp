package ceu.marten.ui;

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
import android.view.Gravity;
import android.view.KeyEvent;
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
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.ui.adapters.ActiveChannelsListAdapter;
import ceu.marten.ui.adapters.ChannelsToDisplayListAdapter;

public class NewConfigActivity extends Activity {

	Configuration config;
	public static final int FREQ_MAX = 1000;
	public static final int FREQ_MIN = 36;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_config);

		/* initialize variables */
		config = new Configuration();
		config.setFrequency(500);
		config.setNumberOfBits(8);
		/* initialize view components */

		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					((TextView) findViewById(R.id.freq_view)).setText(String
							.valueOf(progress + FREQ_MIN));
					config.setFrequency(progress);
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
					if (new_frec > FREQ_MIN && new_frec <= FREQ_MAX) {
						frequency.setProgress((new_frec - FREQ_MIN));
						config.setFrequency(new_frec);
					} else if (new_frec > FREQ_MAX) {
						frequency.setProgress(FREQ_MAX);
						freqView.setText(FREQ_MAX);
						config.setFrequency(FREQ_MAX);
						displayToast("max freq is"+FREQ_MAX+ "Hz");
					} else {
						frequency.setProgress(0);
						freqView.setText(FREQ_MIN);
						config.setFrequency(FREQ_MIN);
						displayToast("min freq is"+FREQ_MIN+" Hz");
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

	}
	//@todo saca las cadena de caracteres de la interfaz de usuario y usa i18n
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
						String[] channelsActivated = acla.getChecked();
						config.setActiveChannels(channelsActivated);
						int counter = 0;
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
		for (int i = 0; i < config.getActiveChannels().length; i++) {
			if (config.getActiveChannels()[i].compareTo("null") != 0) {
				channels.add("channel " + (i + 1));
				sensors.add(config.getActiveChannels()[i]);
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
						boolean[] ctd = new boolean[8];
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
									int in = Character.getNumericValue((channels
											.get(i).toString().charAt(channels
											.get(i).toString().length() - 1)) - 1);
									ctd[in] = true;

									si = si + "\n\t"
											+ channels.get(i).toString()
											+ " with sensor "
											+ sensors.get(i).toString();
								}
							}
							ca.append(si);
							config.setChannelsToDisplay(ctd);
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
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		config.setName(((EditText) findViewById(R.id.dev_name)).getText()
				.toString());
		config.setMacAddress(((EditText) findViewById(R.id.nc_mac_address))
				.getText().toString());
		savedInstanceState.putSerializable("configSaved", config);
		savedInstanceState.putString("actChannels",
				((TextView) findViewById(R.id.txt_canales_activos)).getText()
						.toString());
		savedInstanceState.putString("channelsToShow",
				((TextView) findViewById(R.id.nc_txt_channels_to_show))
						.getText().toString());

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		config = (Configuration) savedInstanceState
				.getSerializable("configSaved");

		((TextView) findViewById(R.id.txt_canales_activos))
				.setText(savedInstanceState.getString("actChannels"));
		((TextView) findViewById(R.id.nc_txt_channels_to_show))
				.setText(savedInstanceState.getString("channelsToShow"));
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
		config.setMacAddress(((EditText) findViewById(R.id.nc_mac_address))
				.getText().toString());
		config.setCreateDate(dateFormat.format(date));

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
				config.setNumberOfBits(8);
			break;
		case R.id.radioBttn12:
			if (checked)
				config.setNumberOfBits(12);
			break;
		}
	}

	public void onClickedChannelPickDialog(View view) {
		setupActiveChannelsDialog();
	}

	public void onClickedChannelDisplayPickDialog(View view) {
		String[] channelsActivated = config.getActiveChannels();
		if (channelsActivated != null) {
			int counter = 0;
			for (int i = 0; i < channelsActivated.length; i++) {
				if (channelsActivated[i].compareTo("null") != 0) {
					counter++;
				}
			}

			if (counter != 0)
				setupChannelsToDisplay();
			else
				displayToast("please select active channels first");
		} else
			displayToast("please select active channels first");
	}
}
