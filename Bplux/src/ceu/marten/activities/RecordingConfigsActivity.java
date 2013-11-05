package ceu.marten.activities;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import ceu.marten.adapters.RecordingConfigListAdapter;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;
import ceu.marten.dataBase.DatabaseHelper;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class RecordingConfigsActivity extends
		OrmLiteBaseActivity<DatabaseHelper> implements OnDismissCallback {

	public AlertDialog RecName;
	private ListView devListView;
	private RecordingConfigListAdapter baseAdapter;
	private ArrayList<Configuration> configs = null;
	private Context RCAcontext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_recording_configs);

		loadDevicesConfig();
		setupRecordingNameDialog();
		setupDevicesListView();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Configuration config = ((Configuration) data
						.getSerializableExtra("config"));

				saveDeviceConfig(config);
				loadDevicesConfig();
				setupDevicesListView();
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
		if (requestCode == 2) {
			if (resultCode == RESULT_OK) {

			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}

	private void setupRecordingNameDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();

		builder.setView(inflater.inflate(R.layout.dialog_recording_name, null))
				.setTitle("New recording name")
				.setPositiveButton("accept",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								EditText et = (EditText) RecName
										.findViewById(R.id.dialog_txt_new_recording_name);
								String newRecordingName = et.getText()
										.toString();

								Intent intent = new Intent(RCAcontext,
										NewRecordingActivity.class);
								intent.putExtra("recordingName",
										newRecordingName);
								startActivityForResult(intent, 2);

							}
						})
				.setNegativeButton("cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// hide dialog
							}
						});
		RecName = builder.create();

	}

	private void setupDevicesListView() {

		/** SETTING UP THE LISTENERS */

		final OnItemClickListener shortPressListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> AdapterView, View v,
					int position, long id) {
				RecName.show();

			}
		};

		devListView = (ListView) findViewById(R.id.lvConfigs);
		devListView.setOnItemClickListener(shortPressListener);

		/** SETTING UP THE ADAPTER */
		baseAdapter = new RecordingConfigListAdapter(this, configs);
		setSwipeToDismissAdapter();

	}

	private void setSwipeToDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
				this);
		swipeAdapter.setAbsListView(devListView);
		devListView.setAdapter(baseAdapter);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			baseAdapter.remove(position);
			Dao<Configuration, Integer> dao = null;

			try {
				dao = getHelper().getDeviceConfigDao();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dao.delete(configs.get(position));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			configs.remove(position);
		}
		Toast.makeText(this, "config. removed ", Toast.LENGTH_SHORT).show();
	}

	public void saveDeviceConfig(Configuration config) {
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			dao.create(config);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadDevicesConfig() {
		configs = new ArrayList<Configuration>();
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			QueryBuilder<Configuration, Integer> builder = dao.queryBuilder();
			builder.orderBy("createDate", false).limit(30L);
			configs = (ArrayList<Configuration>) dao.query(builder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* BUTTON EVENTS */

	public void onClickedNewConfig(View v) {
		Intent intent = new Intent(this, NewConfigActivity.class);
		startActivityForResult(intent, 1);
	}

}