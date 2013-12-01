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

	private static final int FREQUENCY_MAX = 1000;
	private static final int FREQUENCY_MIN = 36;
	private static final int DEFAULT_FREQUENCY = 500;
	private static final int DEFAULT_NUMBER_OF_BITS = 8;

	private SeekBar frequencySeekbar;
	private EditText frequencyEditor, configurationName, macAddress;
	private TextView activeChannels, channelsToDisplay;

	Configuration newConfiguration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_config);

		initializeVariables();
		findViews();
		initializeComponents();

	}

	private void findViews() {
		frequencySeekbar = (SeekBar) findViewById(R.id.freq_seekbar);
		frequencyEditor = (EditText) findViewById(R.id.freq_view);
		configurationName = (EditText) findViewById(R.id.dev_name);
		macAddress = (EditText) findViewById(R.id.nc_mac_address);
		activeChannels = (TextView) findViewById(R.id.txt_canales_activos);
		channelsToDisplay = (TextView) findViewById(R.id.nc_txt_channels_to_show);
	}

	private void initializeVariables() {
		newConfiguration = new Configuration();
		newConfiguration.setFrequency(DEFAULT_FREQUENCY);
		newConfiguration.setNumberOfBits(DEFAULT_NUMBER_OF_BITS);

	}

	private void initializeComponents() {

		frequencySeekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean changedByUser) {
						if (changedByUser) {
							frequencyEditor.setText(String.valueOf(progress
									+ FREQUENCY_MIN));
							newConfiguration.setFrequency(progress + FREQUENCY_MIN);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});

		frequencyEditor.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView currentView, int actionId,
					KeyEvent event) {
				boolean handled = false;

				if (actionId == EditorInfo.IME_ACTION_DONE) {

					int new_frec = Integer.parseInt(currentView.getText()
							.toString());
					if (new_frec >= FREQUENCY_MIN && new_frec <= FREQUENCY_MAX) {
						frequencySeekbar
								.setProgress((new_frec - FREQUENCY_MIN));
						newConfiguration.setFrequency(new_frec);
					} else if (new_frec > FREQUENCY_MAX) {
						frequencySeekbar.setProgress(FREQUENCY_MAX);
						frequencyEditor.setText(FREQUENCY_MAX);
						newConfiguration.setFrequency(FREQUENCY_MAX);
						displayToast("max frequency is " + FREQUENCY_MAX + "Hz");
					} else {
						frequencySeekbar.setProgress(0);
						frequencyEditor.setText(FREQUENCY_MIN);
						newConfiguration.setFrequency(FREQUENCY_MIN);
						displayToast("min frequency is " + FREQUENCY_MIN
								+ " Hz");
					}

					closeKeyboardAndClearFocus();

					handled = true;
				}
				return handled;
			}

			private void closeKeyboardAndClearFocus() {
				frequencyEditor.clearFocus();
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				inputManager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

			}
		});

	}

	// @todo saca las cadena de caracteres de la interfaz de usuario y usa i18n
	private void setupActiveChannelsDialog() {

		final ArrayList<String> channels = new ArrayList<String>();
		channels.add("channel 1");channels.add("channel 2");
		channels.add("channel 3");channels.add("channel 4");
		channels.add("channel 5");channels.add("channel 6");
		channels.add("channel 7");channels.add("channel 8");

		final ActiveChannelsListAdapter activeChannelsListAdapter = 
				new ActiveChannelsListAdapter(this, channels);
		AlertDialog.Builder activeChannelsBuilder;
		AlertDialog activeChannelsDialog;
		ListView activeChannelsListView;

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
						String[] channelsActivated = activeChannelsListAdapter
								.getChecked();
						newConfiguration.setActiveChannels(channelsActivated);
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

		activeChannelsListView = (ListView) activeChannelsDialog
				.findViewById(R.id.lv_channelsSelection);
		activeChannelsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		activeChannelsListView.setItemsCanFocus(false);
		activeChannelsListView.setAdapter(activeChannelsListAdapter);

	}

	private void setupChannelsToDisplay() {

		final ArrayList<String> channels = new ArrayList<String>();
		final ArrayList<String> sensors = new ArrayList<String>();
		for (int i = 0; i < newConfiguration.getActiveChannels().length; i++) {
			if (newConfiguration.getActiveChannels()[i].compareTo("null") != 0) {
				channels.add("channel " + (i + 1));
				sensors.add(newConfiguration.getActiveChannels()[i]);
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
							newConfiguration.setChannelsToDisplay(ctd);
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

		newConfiguration.setName(configurationName.getText().toString());
		newConfiguration.setMacAddress(macAddress.getText().toString());

		savedInstanceState.putSerializable("configuration", newConfiguration);
		savedInstanceState.putString("activeChannels", activeChannels.getText()
				.toString());
		savedInstanceState.putString("channelsToDisplay", channelsToDisplay
				.getText().toString());

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		newConfiguration = (Configuration) savedInstanceState
				.getSerializable("configuration");
		activeChannels.setText(savedInstanceState.getString("activeChannels"));
		channelsToDisplay.setText(savedInstanceState
				.getString("channelsToDisplay"));
	}

	private void displayToast(String messageToDisplay) {
		Toast.makeText(getApplicationContext(), messageToDisplay,
				Toast.LENGTH_SHORT).show();

	}

	public void onClickedSubmit(View view) {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		newConfiguration.setCreateDate(dateFormat.format(date));
		newConfiguration.setName(configurationName.getText().toString());
		newConfiguration.setMacAddress(macAddress.getText().toString());

		Intent returnIntent = new Intent();
		returnIntent.putExtra("config", newConfiguration);
		setResult(RESULT_OK, returnIntent);
		finish();

		displayToast("configuration successfully created");

	}

	public void onClickedCancel(View view) {
		finish();
	}

	public void onRadioButtonClicked(View radioButtonView) {
		boolean checked = ((RadioButton) radioButtonView).isChecked();

		switch (radioButtonView.getId()) {
		case R.id.radioBttn8:
			if (checked)
				newConfiguration.setNumberOfBits(8);
			break;
		case R.id.radioBttn12:
			if (checked)
				newConfiguration.setNumberOfBits(12);
			break;
		}
	}

	public void onClickedChannelPickDialog(View view) {
		setupActiveChannelsDialog();
	}

	public void onClickedChannelDisplayPickDialog(View view) {
		String[] channelsActivated = newConfiguration.getActiveChannels();
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
