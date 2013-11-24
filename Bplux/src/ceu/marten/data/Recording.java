package ceu.marten.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "recordings")
public class Recording implements Serializable {

	private static final long serialVersionUID = -5456569572649294107L;
	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(canBeNull = true)
	private String name;
	@DatabaseField(canBeNull = true)
	private String savedDate;
	@DatabaseField(canBeNull = true)
	private long duration = 0;
	@DatabaseField(canBeNull = true, foreign = true)
	private Configuration config;

	public Recording() {
		// needed by OrmLite
	}

	public Recording(String initName, String initStartDate, long initDuration,
			Configuration initConfig, String initDataId) {
		super();
		this.name = initName;
		this.savedDate = initStartDate;
		this.duration = initDuration;
		this.config = initConfig;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getSavedDate() {
		return savedDate;
	}

	public long getDuration() {
		return duration;
	}

	public void setSavedDate(String startDate) {
		this.savedDate = startDate;
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

	@Override
	public String toString() {
		return "name " + name + "\n " + "startDate " + savedDate + "\n "
				+ "duration " + duration + "\n\t " + config.toString();
	}

}
