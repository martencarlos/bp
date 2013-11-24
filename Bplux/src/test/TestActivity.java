package test;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import ceu.marten.activities.NewConfigActivity;
import ceu.marten.bplux.R;
import ceu.marten.data.Configuration;
import ceu.marten.data.Recording;
import ceu.marten.dataBase.DatabaseHelper;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class TestActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	private Configuration config;
	private List<Recording> sessions;
	private Recording session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ly_test_activity);
		session = new Recording();
		sessions = new ArrayList<Recording>();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				config = ((Configuration) data.getSerializableExtra("config"));
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}

	/** ON CLICKED BUTTONS */

	public void onClickedAddConfig(View view) {
		Intent intent = new Intent(this, NewConfigActivity.class);
		startActivityForResult(intent, 1);
	}

	public void onClickedAddSession(View view) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		session.setConfig(config);
		session.setDuration(1000);
		session.setName("session 1");
		session.setStartDate(dateFormat.format(date));
	}

	public void onClickedSaveSession(View view) {
		saveDeviceConfig(config);
		saveSession(session);

	}

	public void onClickedLoadSessions(View view) {

		sessions = loadSessions();
		TextView t = (TextView) findViewById(R.id.txtResult);

		t.setText("Sessions:\n\n");
		for (Recording s : sessions) {

			try {
				Configuration dev = s.getConfig();
				getHelper().getDeviceConfigDao().refresh(dev);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			t.append(s.toString() + "\n");
		}

	}

	public void saveDeviceConfig(Configuration config) {
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			dao.create(config);
		} catch (SQLException e) {
			Log.d("save", "exception al guardar DeviceConfig");
			e.printStackTrace();
		}
	}

	public ArrayList<Configuration> loadDevicesConfig() {
		ArrayList<Configuration> devicesConfig = new ArrayList<Configuration>();
		Dao<Configuration, Integer> dao;
		try {
			dao = getHelper().getDeviceConfigDao();
			devicesConfig = (ArrayList<Configuration>) dao.queryForAll();
		} catch (SQLException e) {
			Log.d("load", "exception sql al cargar config de dispositivos");
			e.printStackTrace();
		}
		return devicesConfig;
	}

	public void saveSession(Recording session) {
		try {
			Dao<Recording, Integer> dao = getHelper().getSessionDao();
			dao.create(session);

		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("save", "exception al guardar Session en BD");
		}

	}

	public List<Recording> loadSessions() {
		Dao<Recording, Integer> dao;
		List<Recording> sessions = new ArrayList<Recording>();
		try {
			dao = getHelper().getSessionDao();
			QueryBuilder<Recording, Integer> builder = dao.queryBuilder();
			builder.orderBy("startDate", false).limit(30L);
			sessions = dao.query(builder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("save", "exception al cargar");
		}
		return sessions;
	}

}
