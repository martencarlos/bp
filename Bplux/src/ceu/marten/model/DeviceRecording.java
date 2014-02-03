package ceu.marten.model;

import java.io.Serializable;

import ceu.marten.model.io.DatabaseHelper;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a recording of a session with a specific bioplux device configuration
 * Stored in Android's internal Database with ORMlite annotations 
 * Implements Serializable to transfer instances between activities
 * 
 *  If this class is updated. Make sure to update also database version in {@link DatabaseHelper}
 * 
 * @author Carlos Marten
 */

@DatabaseTable(tableName = "DeviceRecordings")
public class DeviceRecording implements Serializable {

	// Used for unique serializable purposes
	private static final long serialVersionUID = -5456569572649294107L;
	public  static final String DATE_FIELD_NAME = "startDate";
	
	@DatabaseField(generatedId = true)
	private Integer id;
	
	@DatabaseField(unique=true, canBeNull = true)
	private String name;
	
	@DatabaseField(canBeNull = true)
	private String startDate;
	
	@DatabaseField(canBeNull = true)
	private String duration;
	
	@DatabaseField(canBeNull = true, foreign = true)
	private DeviceConfiguration configuration;

	/**
	 * Constructor Needed for the OrmLite to generate object when query invoked
	 */
	public DeviceRecording() {}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setSavedDate(String startDate) {
		this.startDate = startDate;
	}

	public String getSavedDate() {
		return startDate;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDuration() {
		return duration;
	}

	public void setConfiguration(DeviceConfiguration newConfiguration) {
		this.configuration = newConfiguration;
	}

	public DeviceConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public String toString() {
		return "name " + name + "\n " + "startDate " + startDate + "\n "
				+ "duration " + duration + "\n\t ";
	}
}
