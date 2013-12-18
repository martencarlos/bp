package ceu.marten.ui;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.model.Recording;
import ceu.marten.model.io.DatabaseHelper;
import ceu.marten.ui.adapters.ConfigurationsListAdapter;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class ConfigurationsActivity extends OrmLiteBaseActivity<DatabaseHelper>
		implements OnDismissCallback {

	private static final String TAG = ConfigurationsActivity.class.getName();

	private AlertDialog recordingNameDialog;
	private AlertDialog confirmationNameDialog;
	private ListView configurationsListView;
	private ConfigurationsListAdapter baseAdapter;
	private ArrayList<Configuration> configurations = null;
	private ArrayList<Recording> recordings = null;
	private Context classContext = this;
	private int currentConfigurationsPosition = 0;
	private int[] reverseSortedPositions;
	private LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_configurations);
		inflater = this.getLayoutInflater();
		loadConfigurations();
		setupConfigurationsListView();
		setupRecordingNameDialog();
		setupConfirmationDialog();
	}
	
	@Override
	public void onBackPressed() {
		Intent backIntent = new Intent(this, HomeActivity.class);
		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(backIntent);
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right );
	    super.onBackPressed();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {

				Configuration configuration = ((Configuration) data
						.getSerializableExtra("configuration"));
				saveConfiguration(configuration);
				loadConfigurations();
				setupConfigurationsListView();
			}
			if (resultCode == RESULT_CANCELED) {

			}
		}
	}

	private void setupRecordingNameDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		
		TextView customTitleView = (TextView)inflater.inflate(R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.ca_name_dialog_title);
		builder.setView(inflater.inflate(R.layout.dialog_recording_name_content, null))
				.setCustomTitle(customTitleView);
		recordingNameDialog = builder.create();
		
	}
	
	private void setupConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView customTitleView = (TextView)inflater.inflate(R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.ca_confirm_dialog_title);
		builder.setCustomTitle(customTitleView)
		.setMessage(R.string.ca_confirm_dialog_message)
		.setPositiveButton(getString(R.string.ca_confirm_dialog_positive),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						for (int position : reverseSortedPositions) {
							baseAdapter.remove(position);
							Dao<Configuration, Integer> dao = null;

							try {
								dao = getHelper().getDeviceConfigDao();
								dao.delete(configurations.get(position));
							} catch (SQLException e) {
								Log.e(TAG,"exception removing configuration from database by swiping",
										e);
							}
							configurations.remove(position);
						}
						displayInfoToast(getString(R.string.ca_configuration_removed));
					}
				});
		builder.setNegativeButton(getString(R.string.ca_confirm_dialog_negative),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						setupConfigurationsListView();
					}
				});

		confirmationNameDialog = builder.create();
		confirmationNameDialog.setCanceledOnTouchOutside(false);
	}
	
	private void setupConfigurationsListView() {

		final OnItemClickListener shortPressListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> AdapterView, View v,
					int position, long id) {
				currentConfigurationsPosition = position;
				recordingNameDialog.show();

			}
		};

		configurationsListView = (ListView) findViewById(R.id.lvConfigs);
		configurationsListView.setOnItemClickListener(shortPressListener);
		configurationsListView
				.setEmptyView(findViewById(R.id.empty_list_configurations));

		/** SETTING UP THE ADAPTER */
		baseAdapter = new ConfigurationsListAdapter(this, configurations);
		setSwipeToDismissAdapter();

	}

	private void setSwipeToDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
				this);
		swipeAdapter.setAbsListView(configurationsListView);
		configurationsListView.setAdapter(baseAdapter);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		this.reverseSortedPositions = reverseSortedPositions;
		confirmationNameDialog.show();
	}

	private void displayInfoToast(String messageToDisplay) {
		Toast infoToast = new Toast(getApplicationContext());

		LayoutInflater inflater = getLayoutInflater();
		View toastView = inflater.inflate(R.layout.toast_info, null);
		infoToast.setView(toastView);
		((TextView) toastView.findViewById(R.id.display_text))
				.setText(messageToDisplay);

		infoToast.show();
	}

	public void saveConfiguration(Configuration config) {
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			dao.create(config);
		} catch (SQLException e) {
			Log.e(TAG, "exception saving configuration on database", e);
		}
	}

	public void loadConfigurations() {
		configurations = new ArrayList<Configuration>();
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			QueryBuilder<Configuration, Integer> builder = dao.queryBuilder();
			builder.orderBy("createDate", false).limit(30L);
			configurations = (ArrayList<Configuration>) dao.query(builder
					.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "exception loading configurations from database ", e);
		}
	}
	
	public void loadRecordings() {
		Dao<Recording, Integer> dao;
		recordings= new ArrayList<Recording>();
		try {
			dao = getHelper().getRecordingDao();
			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
			builder.orderBy("startDate", false).limit(30L);
			recordings = (ArrayList<Recording>) dao.query(builder
					.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "exception loading recordings from database ", e);
		}
	}

	/* BUTTON EVENTS */

	public void onClickedNewConfig(View v) {
		Intent intent = new Intent(this, NewConfigurationActivity.class);
		intent.putExtra("configurations", configurations);
		startActivityForResult(intent, 1);
		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
	}
	
	public void onNegativeClick(View v) {
		EditText editText = (EditText) recordingNameDialog.findViewById(R.id.dialog_txt_new_recording_name);
		editText.setError(null);
		recordingNameDialog.dismiss();
	}
	
	
	public void onPositiveClick(View v) {
		EditText editText = (EditText) recordingNameDialog.findViewById(R.id.dialog_txt_new_recording_name);
		String newRecordingName = editText.getText().toString();
		loadRecordings();
		boolean recordingNameExists = false;
		if(recordings!=null){
			for(Recording r : recordings){
				if(r.getName().compareTo(newRecordingName)==0)
					recordingNameExists = true;
			}
		}
		
		if (newRecordingName == null || newRecordingName.compareTo("") == 0) {
			editText.setError(getString(R.string.ca_dialog_null_name));
		}else if(recordingNameExists){
			editText.setError(getString(R.string.ca_dialog_duplicate_name));
		}else{
			Intent intent = new Intent(classContext,NewRecordingActivity.class);
			intent.putExtra("recordingName",newRecordingName);
			intent.putExtra("configSelected",configurations.get(currentConfigurationsPosition));
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			recordingNameDialog.dismiss();
		}
	}
}