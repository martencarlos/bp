
package ceu.marten.model;

import java.io.Serializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martencarlos on 25/07/13.
 */

@DatabaseTable(tableName = "deviceConfigurations")
public class Configuration implements Serializable {

	private static final long serialVersionUID = -4487071327586521666L;
	@DatabaseField(generatedId = true)
	private Integer id;

	@DatabaseField(canBeNull = true)
	private String name = null;
	@DatabaseField(canBeNull = true)
	private String macAddress = null;
	@DatabaseField(canBeNull = true)
	private String createDate = null;
	@DatabaseField(canBeNull = true)
	private int frequency = 0;
	@DatabaseField(canBeNull = true)
	private int numberOfBits = 8; // number of bits can be 8 or 12 [0-255] | [0-4095]

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] activeChannels = null;
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] channelsToDisplay = null;

	public Configuration() {
		// needed for the OrmLite to generate object when query invoked
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getNumberOfBits() {
		return numberOfBits;
	}

	public void setNumberOfBits(int numberOfBits) {
		this.numberOfBits = numberOfBits;
	}

	public void setChannelsToDisplay(boolean[] boo) {
		int iterator = 0;
		String[] channelsToDisplay = new String[8];
		for (boolean b : boo) {
			if (b)
				channelsToDisplay[iterator] = "true";
			else
				channelsToDisplay[iterator] = "false";
			iterator++;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < channelsToDisplay.length; i++) {
			sb.append(channelsToDisplay[i]);
			if (i != channelsToDisplay.length - 1) {
				sb.append("*.*"); // concatenate by this splitter
			}
		}
		this.channelsToDisplay = sb.toString().getBytes();
	}

	public boolean[] getChannelsToDisplay() {
		String entire = new String(this.channelsToDisplay);
		String[] channelsToDisplay = entire.split("\\*\\.\\*");
		int iterator = 0;
		boolean[] result = new boolean[8];
		for (String s : channelsToDisplay) {
			if (s.equalsIgnoreCase("true"))
				result[iterator] = true;
			else
				result[iterator] = false;
			iterator++;
		}
		return result;
	}

	public void setActiveChannels(String[] activeChannels) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < activeChannels.length; i++) {
			sb.append(activeChannels[i]);
			if (i != activeChannels.length - 1) {
				sb.append("*.*"); // concatenate by this splitter
			}
		}
		this.activeChannels = sb.toString().getBytes();
	}

	public String[] getActiveChannels() {
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			return entire.split("\\*\\.\\*");
		} else
			return null;
	}

	public String getActiveChannelsAsString() {
		String strActChnls = "";
		String[] arrayStrings;
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			arrayStrings = entire.split("\\*\\.\\*");
			for (int i = 0; i < arrayStrings.length; i++) {
				if (arrayStrings[i].compareToIgnoreCase("null") != 0)
					strActChnls += "\t" + "channel " + (i + 1) + " with sensor " + arrayStrings[i]
							+ "\n";
			}
			return strActChnls;
		} else
			return null;
		
	}

	@Override
	public String toString() {
		return "name " + name + "; " + "freq " + frequency + "; " + "nBits " + numberOfBits
				+ "; " + "\n Active channels ";// +activeChannelsToString()+"\n";
	}
}
