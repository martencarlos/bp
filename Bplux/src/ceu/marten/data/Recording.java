package ceu.marten.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sessions")
public class Recording implements Serializable {

	private static final long serialVersionUID = -5456569572649294107L;
	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(canBeNull = true)
	private String name;
	@DatabaseField(canBeNull = true)
	private String startDate;
	@DatabaseField(canBeNull = true)
	private long duration = 0;
	@DatabaseField(canBeNull = true, foreign = true)
	private Configuration config;

	@DatabaseField(canBeNull = true)
	private String data_id;

	public Recording() {
		// needed by OrmLite
	}

	public Recording(String initName, String initStartDate, long initDuration,
			Configuration initConfig, String initDataId) {
		super();
		this.name = initName;
		this.startDate = initStartDate;
		this.duration = initDuration;
		this.config = initConfig;
		this.data_id = initDataId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getStartDate() {
		return startDate;
	}

	public long getDuration() {
		return duration;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setData_id(String data_id) {
		this.data_id = data_id;
	}

	public String getData_id() {
		return data_id;
	}

	@Override
	public String toString() {
		return "name " + name + "\n " + "startDate " + startDate + "\n "
				+ "duration " + duration + "\n\t " + config.toString();
	}

}
