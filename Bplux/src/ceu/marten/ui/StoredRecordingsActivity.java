package ceu.marten.ui;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bplux.R;
import ceu.marten.model.Recording;
import ceu.marten.model.io.DatabaseHelper;
import ceu.marten.ui.adapters.StoredRecordingsListAdapter;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class StoredRecordingsActivity extends
		OrmLiteBaseActivity<DatabaseHelper> implements OnDismissCallback {

	private static final String TAG = StoredRecordingsActivity.class.getName();
	
	private ListView lv_recordings;
	private StoredRecordingsListAdapter baseAdapter;
	private ArrayList<Recording> recordingsArrayList = null;
	private Context myContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_stored_recordings);
		//@todo ¿esto no debería hacerse de modo asíncrono?
		loadRecordings();
		setupRecordingListView();
	}


	private void setupRecordingListView() {
		
		final OnItemClickListener shortPressListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> AdapterView, View v,
					int position, long id) {
				TextView tv = (TextView) v.findViewById(R.id.dli_name);
				sendDataViaEmail(tv.getText().toString());
				/*Intent intent = new Intent(myContext, RecordingViewActivity.class);
				intent.putExtra("recordingName", tv.getText().toString());
				startActivity(intent);*/
			}
		};
		lv_recordings = (ListView) findViewById(R.id.lvSessions);
		lv_recordings.setOnItemClickListener(shortPressListener);
		baseAdapter = new StoredRecordingsListAdapter(this, recordingsArrayList);
		setSwipeToDismissAdapter();

	}
	public void sendDataViaEmail(String recordingName) {
		for(int i=0;i<fileList().length;i++)
			Log.d(TAG, fileList()[i]);
		
		File F = new File(getFilesDir()+"/"+recordingName + ".zip");
		Uri U = Uri.fromFile(F);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/rtf");
		i.putExtra(Intent.EXTRA_STREAM, U);
		startActivity(Intent.createChooser(i, "select email client"));
	}

	private void setSwipeToDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
				this);
		swipeAdapter.setAbsListView(lv_recordings);
		lv_recordings.setAdapter(baseAdapter);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			baseAdapter.remove(position);
			Dao<Recording, Integer> dao = null;

			try {
				dao = getHelper().getRecordingDao();
				dao.delete(recordingsArrayList.get(position));
			} catch (SQLException e) {
				Log.e(TAG, "Exception removing recording from database ",e);
			}
			recordingsArrayList.remove(position);
		}
		Toast.makeText(this, "recording removed ", Toast.LENGTH_SHORT).show();
	}

	public void loadRecordings() {
		Dao<Recording, Integer> dao;
		recordingsArrayList = new ArrayList<Recording>();
		try {
			dao = getHelper().getRecordingDao();
			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
			builder.orderBy("startDate", false).limit(30L);
			recordingsArrayList = (ArrayList<Recording>) dao.query(builder.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "exception loading recordings from database ",e);
		}
	}

}
