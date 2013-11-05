package ceu.marten.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;

public class NewConfigActivity extends Activity {

	String[] channelsSelected;
	int freq;
	int nbits;
	Configuration config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_new_config);

		/* initialize view components */
		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView) findViewById(R.id.freq_view)).setText(String
						.valueOf(progress + " Hz"));
				freq = progress;
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});


		/* initialize variables */
		config = new Configuration();
		
		channelsSelected = new String[8];
		for(int i=0; i<channelsSelected.length;i++)
			channelsSelected[i]=null;
			
	}

	
	@Override
	public Dialog onCreateDialog(int s) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("select active channels")
	           .setMultiChoiceItems(R.array.channelList, null,
	                      new DialogInterface.OnMultiChoiceClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int pos,
	                       boolean isChecked) {
	                   if (isChecked) {
	                	   channelsSelected[pos]="activo";
	                   } else if (channelsSelected[pos] =="activo") {
	                	   channelsSelected[pos]= null;
	                   }
	               }
	           })
	           .setPositiveButton("ok", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   TextView ca = (TextView)findViewById(R.id.txt_canales_activos);
	                   ca.setText(" ");
	                   for(int i=0; i<channelsSelected.length; i++){
	                	   if(channelsSelected[i] == "activo") 
	                		   	ca.append("channel "+(i+1)+", ");
	                   }  
	                   ca.append("activated");
	                   
	                   config.setActiveChannels(channelsSelected);
	                   
	               }
	           })
	           .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   for(int i=0; i<channelsSelected.length;i++)
	            		   channelsSelected[i] = null;
	               }
	           });

	    return builder.create();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	public void onClickedSubmit(View view) {

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		config.setName(((EditText) findViewById(R.id.dev_name)).getText()
				.toString());
		config.setFreq(freq);
		config.setnBits(nbits);
		config.setActiveChannels(channelsSelected);
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
				nbits = 8;
			break;
		case R.id.radioBttn12:
			if (checked)
				nbits = 12;
			break;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void onClickedChannelPickDialog(View view){
		showDialog(123);
	}
	
}
