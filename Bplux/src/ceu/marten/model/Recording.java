package ceu.marten.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "recordings")
public class Recording implements Serializable {

	private static final long serialVersionUID = -5456569572649294107L;
	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(unique=true, canBeNull = true)
	private String name;
	@DatabaseField(canBeNull = true)
	private String startDate;
	@DatabaseField(canBeNull = true)
	private String duration;
	@DatabaseField(canBeNull = true, foreign = true)
	private Configuration config;

	public Recording() {
		// needed by OrmLite
	}

	public Recording(String initName, String initStartDate,
			String initDuration, Configuration initConfig, String initDataId) {
		super();
		this.name = initName;
		this.startDate = initStartDate;
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
		return startDate;
	}

	public String getDuration() {
		return duration;
	}

	public void setSavedDate(String startDate) {
		this.startDate = startDate;
	}

	public void setDuration(String duration) {
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
		return "name " + name + "\n " + "startDate " + startDate + "\n "
				+ "duration " + duration + "\n\t " + config.toString();
	}

}
