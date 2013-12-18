package ceu.marten.model;

import java.io.Serializable;
import java.util.ArrayList;
import android.util.Log;

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

	@DatabaseField(unique=true, canBeNull = true)
	private String name = null;
	@DatabaseField(canBeNull = true)
	private String macAddress = null;
	@DatabaseField(canBeNull = true)
	private String createDate = null;
	@DatabaseField(canBeNull = true)
	private int receptionFrequency = 0;
	@DatabaseField(canBeNull = true)
	private int samplingFrequency = 0;
	@DatabaseField(canBeNull = true)
	private int numberOfBits = 8; // number of bits can be 8 or 12 [0-255] |
									// [0-4095]

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

	public void setReceptionFrequency(int frequency) {
		this.receptionFrequency = frequency;
	}

	public int getReceptionFrequency() {
		return receptionFrequency;
	}

	public int getSamplingFrequency() {
		return samplingFrequency;
	}

	public void setSamplingFrequency(int samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
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

	public ArrayList<Integer> getChannelsToDisplay() {
		String entire = new String(this.channelsToDisplay);
		String[] channelsToDisplay = entire.split("\\*\\.\\*");
		ArrayList<Integer> result = new ArrayList<Integer>();
		int channelNumber = 1;
		for (String s : channelsToDisplay) {
			if (s.equalsIgnoreCase("true"))
				result.add(channelNumber);
			channelNumber++;
		}
		return result;
	}

	public int getNumberOfChannelsToDisplay() {
		int numberOfChannelsToDisplay = 0;
		String entire = new String(this.channelsToDisplay);
		String[] channelsToDisplay = entire.split("\\*\\.\\*");
		for (String s : channelsToDisplay) {
			if (s.equalsIgnoreCase("true"))
				numberOfChannelsToDisplay++;
		}
		return numberOfChannelsToDisplay;
	}

	public void setActiveChannels(String[] activeChannels) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < activeChannels.length; i++) {
			sb.append(activeChannels[i]);
			if (i != activeChannels.length - 1) {
				sb.append("*.*");
			}
		}
		this.activeChannels = sb.toString().getBytes();
	}

	public String[] getActiveChannelsWithNullFill() {
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			return entire.split("\\*\\.\\*");
		} else
			return null;
	}

	public String getActiveChannelsAsString() {
		String activeChannels = "";
		String[] arrayStrings;
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			arrayStrings = entire.split("\\*\\.\\*");
			for (int i = 0; i < arrayStrings.length; i++) {
				if (arrayStrings[i].compareToIgnoreCase("null") != 0)
					activeChannels += (" " + String.valueOf(i + 1) + ",");
			}
			activeChannels = (activeChannels.substring(0,
					activeChannels.length() - 1));
			return activeChannels;
		} else
			return null;
	}

	public ArrayList<Integer> getActiveChannels() {
		ArrayList<Integer> activatedChannels = new ArrayList<Integer>();
		String[] arrayStrings;
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			arrayStrings = entire.split("\\*\\.\\*");
			for (int i = 0; i < arrayStrings.length; i++) {
				if (arrayStrings[i].compareToIgnoreCase("null") != 0)
					activatedChannels.add(i + 1);
			}
			return activatedChannels;
		} else
			return null;
	}

	public int getActiveChannelsAsInteger() {
		int activeChannels = 0;
		String[] arrayStrings;
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			arrayStrings = entire.split("\\*\\.\\*");
			for (int i = 0; i < arrayStrings.length; i++) {
				if (arrayStrings[i].compareToIgnoreCase("null") != 0)
					activeChannels += Math.pow(2, i);
			}
			Log.d("BiopluxService", "activeChannels integer number: "
					+ activeChannels);
			return activeChannels;
		} else
			return 0;

	}

	public int getNumberOfChannelsActivated() {
		int numberOfChannels = 0;
		if (this.activeChannels != null) {
			String entire = new String(this.activeChannels);
			String[] arrayStrings = entire.split("\\*\\.\\*");
			for (int i = 0; i < arrayStrings.length; i++) {
				if (arrayStrings[i].compareToIgnoreCase("null") != 0)
					numberOfChannels++;
			}
			return numberOfChannels;
		} else
			return 0;

	}

	@Override
	public String toString() {
		return "name " + name + "; " + "freq " + receptionFrequency + "; " + "nBits "
				+ numberOfBits + "; " + "\n Active channels ";
	}
}
