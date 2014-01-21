package ceu.marten.ui;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Recording;
import ceu.marten.model.io.DatabaseHelper;
import ceu.marten.ui.adapters.RecordingsListAdapter;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class RecordingsActivity extends OrmLiteBaseActivity<DatabaseHelper>
		implements OnDismissCallback{

	private static final String TAG = RecordingsActivity.class.getName();

	private ListView lvRecordings;
	private String recordingName;
	private RecordingsListAdapter baseAdapter;
	private ArrayList<Recording> recordingsArrayList = null;
	private int[] reverseSortedPositions;
	private SharedPreferences.Editor prefEditor = null;
	private LayoutInflater inflater;
	
	//DIALOGS
	private AlertDialog confirmDialog, errorDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_recordings);
		
		inflater = this.getLayoutInflater();
		if(loadRecordings()){
			setupRecordingListView();
			setupConfirmationDialog();
		}
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	private void setupConfirmationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		
		TextView customTitleView = (TextView)inflater.inflate(R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.ra_confirm_dialog_title);
		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
		
		View contentView = inflater.inflate(R.layout.dialog_confirmation_content, null);
		((TextView)contentView.findViewById(R.id.confirmation_message)).setText(getResources().getString(R.string.ra_confirm_dialog_message));
		
		builder.setCustomTitle(customTitleView)
		.setView(contentView)
		.setPositiveButton(getString(R.string.ra_confirm_dialog_positive),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						loop:
						for (int position : reverseSortedPositions) {
							
							if(!deleteRecording(recordingsArrayList.get(position))){
								break loop;
							}
							
							File root = Environment.getExternalStorageDirectory();
							recordingName = recordingsArrayList.get(position).getName();
							String appDirectory="/Bioplux/";
							File file = new File(root + appDirectory + recordingName + ".zip");
							
							if(file.exists() && !file.delete()){
								setupErrorDialog(getResources().getString(R.string.ra_error_message_deleting_recording_from_filesystem));
								break loop;
							}
							baseAdapter.remove(position);
							recordingsArrayList.remove(position);
							displayInfoToast(getString(R.string.ra_recording_removed));
						}
						if(prefEditor!=null){
							prefEditor.commit();
							setupConfirmationDialog();
							prefEditor = null;
						}
						
					}
				});
		builder.setNegativeButton(getString(R.string.ra_confirm_dialog_negative),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 setupRecordingListView();
						 setupConfirmationDialog();
					}
				});

		confirmDialog = builder.create();
		confirmDialog.setCanceledOnTouchOutside(false);
	}
	
	private void setupErrorDialog(String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		TextView customTitleView = (TextView) inflater.inflate(
				R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.ca_error_dialog_title);
		customTitleView.setBackgroundColor(getResources().getColor(
				R.color.error_dialog));

		View contentView = inflater.inflate(
				R.layout.dialog_confirmation_content, null);
		((TextView) contentView.findViewById(R.id.confirmation_message))
				.setText(errorMessage);

		builder.setCustomTitle(customTitleView)
				.setView(contentView)
				.setPositiveButton(getString(R.string.bp_positive_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});
		errorDialog = builder.create();
		errorDialog.setCanceledOnTouchOutside(false);
	}
	
	private void showFilePathDialog() {
		
		//FILE PATH DIALOG BUILDER
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView customTitleView = (TextView) inflater.inflate(
				R.layout.dialog_custom_title, null);
		customTitleView.setText(R.string.ra_display_path_dialog_title);
		customTitleView.setBackgroundColor(getResources().getColor(
				R.color.error_dialog));

		builder.setCustomTitle(customTitleView)
				.setMessage(R.string.ra_display_path_dialog_message)
				.setPositiveButton(getString(R.string.bp_positive_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								//JUST CLOSES THE DIALOG
							}
						});
		errorDialog = builder.create();
		errorDialog.setCanceledOnTouchOutside(false);
		errorDialog.show();
	}

	private void setupRecordingListView() {

		final OnItemClickListener shortPressListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> AdapterView, View v,
					int position, long id) {
				recordingName = ((TextView) v.findViewById(R.id.dli_name))
						.getText().toString();
				if(fileSizeBiggerThan20Mb())
					showFilePathDialog();
				else
					sendDataViaEmail();
			}
		};
		lvRecordings = (ListView) findViewById(R.id.lvSessions);
		lvRecordings.setOnItemClickListener(shortPressListener);
		lvRecordings.setEmptyView(findViewById(R.id.empty_list_recordings));
		baseAdapter = new RecordingsListAdapter(this, recordingsArrayList);
		setSwipeToDismissAdapter();
	}

	public void sendDataViaEmail() {
		File root = Environment.getExternalStorageDirectory();
		String appDirectory="/Bioplux/";
		File F = new File(root + appDirectory + recordingName + ".zip");
		Uri U = Uri.fromFile(F);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("application/zip");
		i.putExtra(Intent.EXTRA_STREAM, U);
		startActivity(Intent.createChooser(i, getString(R.string.ra_dialog_email)));

	}
	
	public boolean fileSizeBiggerThan20Mb() {
		File root = Environment.getExternalStorageDirectory();
		String appDirectory="/Bioplux/";
		File zipFile = new File(root + appDirectory + recordingName + ".zip");
		if((zipFile.length()/1024d)/1024d > 20.0d)
			return true;
		else
			return false;
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

	private void setSwipeToDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
				this);
		swipeAdapter.setAbsListView(lvRecordings);
		lvRecordings.setAdapter(baseAdapter);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		this.reverseSortedPositions = reverseSortedPositions;
		boolean dontAskForConfrmation = false;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		dontAskForConfrmation = sharedPref.getBoolean(SettingsActivity.KEY_PREF_CONF_REC, false);
		
		if(!dontAskForConfrmation){
			confirmDialog.show();
		}else{
			for (int position : reverseSortedPositions) {
				baseAdapter.remove(position);
				Dao<Recording, Integer> dao = null;

				try {
					dao = getHelper().getRecordingDao();
					dao.delete(recordingsArrayList.get(position));
				} catch (SQLException e) {
					Log.e(TAG, "Exception removing recording from database ", e);
				}
				
				File root = Environment.getExternalStorageDirectory();
				recordingName = recordingsArrayList.get(position).getName();
				String appDirectory="/Bioplux/";
				File file = new File(root + appDirectory + recordingName + ".zip");
				file.delete();
				recordingsArrayList.remove(position);
			}
			displayInfoToast(getString(R.string.ra_recording_removed));
		}
	}

	public boolean loadRecordings() {
		Dao<Recording, Integer> dao;
		recordingsArrayList = new ArrayList<Recording>();
		try {
			dao = getHelper().getRecordingDao();
			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
			builder.orderBy("startDate", false).limit(30L);
			recordingsArrayList = (ArrayList<Recording>) dao.query(builder
					.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "exception loading recordings from database ", e);
			setupErrorDialog(getResources().getString(R.string.ca_error_loading_recordings_message));
			return false;
		}
		return true;
	}
	
	public boolean deleteRecording(Recording recordingToDelete) {
		Dao<Recording, Integer> dao = null;
		try {
			dao = getHelper().getRecordingDao();
			dao.delete(recordingToDelete);
		} catch (SQLException e) {
			Log.e(TAG, "Exception removing recording from database ", e);
			setupErrorDialog(getResources().getString(R.string.ra_error_message_deleting_recording_from_database));
			return false;
		}
		return true;
	}
	
	public void onDialogCheckBoxClicked(View v) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		prefEditor = sharedPref.edit();
		
		if(((CheckBox)v).isChecked())
			prefEditor.putBoolean(SettingsActivity.KEY_PREF_CONF_REC, true);
		else{
			prefEditor.putBoolean(SettingsActivity.KEY_PREF_CONF_REC, false);
			prefEditor=null;
		}
	}
	

}
