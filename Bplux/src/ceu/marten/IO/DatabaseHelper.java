package ceu.marten.IO;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ceu.marten.data.Configuration;
import ceu.marten.data.Recording;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs
 * for the app.
 * 
 * @author Carlos Marten
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "data.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Configuration, Integer> deviceConfigDao;
	private Dao<Recording, Integer> sessionDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);// ,
																// R.raw.ormlite_config
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Configuration.class);
			TableUtils.createTable(connectionSource, Recording.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbase", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase,
			ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Configuration.class, true);
			TableUtils.dropTable(connectionSource, Recording.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(),
					"Unable to upgrade database from version " + oldVer
							+ " to new " + newVer, e);
		}
	}

	public Dao<Configuration, Integer> getDeviceConfigDao() throws SQLException {
		if (deviceConfigDao == null) {
			deviceConfigDao = getDao(Configuration.class);
		}
		return deviceConfigDao;
	}

	public Dao<Recording, Integer> getRecordingDao() throws SQLException {
		if (sessionDao == null) {
			sessionDao = getDao(Recording.class);
		}
		return sessionDao;
	}
}
