package ceu.marten.activities;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.adapters.RecordingConfigListAdapter;
import ceu.marten.adapters.StoredRecordingsListAdapter;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;
import ceu.marten.data.Recording;
import ceu.marten.dataBase.DatabaseHelper;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class StoredRecordingsActivity extends OrmLiteBaseActivity<DatabaseHelper> implements
		OnDismissCallback {

	private Dialog dialog;
	private ListView lv_sessions;
	private StoredRecordingsListAdapter baseAdapter;
	private ArrayList<Recording> al_sessions = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_stored_recordings);
		
		loadSessions();
		setupSessionDetailsDialog();
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


	private void setupSessionDetailsDialog() {

		dialog = new Dialog(this);
		dialog.setTitle("Session details");
	}

	private void setupDevicesListView() {

		/** SETTING UP THE LISTENERS */
		

		lv_sessions = (ListView) findViewById(R.id.lvSessions);

		/** SETTING UP THE ADAPTERS */
		baseAdapter = new StoredRecordingsListAdapter(this, al_sessions);
		setSwipeToDismissAdapter();
		
	}

	private void setSwipeToDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
				this);
		swipeAdapter.setAbsListView(lv_sessions);
		lv_sessions.setAdapter(baseAdapter);
	}
	

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			baseAdapter.remove(position);
			Dao<Recording, Integer> dao = null;
			
			try {
				 dao = getHelper().getSessionDao();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dao.delete(al_sessions.get(position));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			al_sessions.remove(position);
		}
		Toast.makeText(this, "session removed ", Toast.LENGTH_SHORT).show();
	}

	

	
	public void loadSessions() {
		Dao<Recording, Integer> dao;
		al_sessions = new ArrayList<Recording>();
		try {
			dao = getHelper().getSessionDao();
			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
			builder.orderBy("startDate", false).limit(30L);
			al_sessions = (ArrayList<Recording>) dao.query(builder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	


}