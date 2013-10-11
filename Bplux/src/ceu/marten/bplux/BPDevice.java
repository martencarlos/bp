package ceu.marten.bplux;

import java.io.Serializable;

import plux.android.bioplux.BPException;
import plux.android.bioplux.Device;

/**
 * Created by martencarlos on 25/07/13.
 */
public class BPDevice implements Serializable {

	private static final long serialVersionUID = 1L;
	private Device connection = null;
	private String name = null;
	private Device.Frame[] frames = null;
	private String description = null;
	private float freq = 0;
	private int channel = 0;
	private int nBits = 0; // number of bits can be 8 or 12 [0-255] | [0-4095]
	private boolean digOutput = false;
	
	private Session[] sessions;
	private boolean isSimDevice = false;
	private boolean isConnected = false;

	public BPDevice() {
		/*
		 * frames = new Device.Frame[1];
		 * 
		 * //initialize frames array for (int i = 0; i < frames.length; i++) {
		 * frames[i] = new Device.Frame(); }
		 * 
		 * //bioPlux initialization try { device =
		 * Device.Create("test");//Device mac addr 00:07:80:4C:2A:FB } catch
		 * (BPException e) { e.printStackTrace(); Log.d("BPexception",
		 * e.getMessage()); }
		 */

	}

	@SuppressWarnings("unused")
	private double getFrame(int n) {
		try {
			connection.GetFrames(1, frames);
		} catch (BPException e) {
			e.printStackTrace();
		}

		return (double) frames[0].an_in[n];
	}

	public void beginAcq() {
		try {
			connection.BeginAcq();
		} catch (BPException e) {
			e.printStackTrace();
		}
	}

	// GETTERS AND SETTERS

	public Device.Frame[] getFrames() {
		return frames;
	}

	public void setFrames(Device.Frame[] frames) {
		this.frames = frames;
	}

	public Device getConnection() {
		return connection;
	}

	public void setConnection(Device connection) {
		this.connection = connection;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getFreq() {
		return freq;
	}

	public void setFreq(float freq) {
		this.freq = freq;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getnBits() {
		return nBits;
	}

	public void setnBits(int nBits) {
		this.nBits = nBits;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSimDevice() {
		return isSimDevice;
	}

	public void setisSimDevice(boolean simDevice) {
		this.isSimDevice = simDevice;
	}

	public boolean isDigOutput() {
		return digOutput;
	}

	public void setDigOutput(boolean digOutput) {
		this.digOutput = digOutput;
	}

	public void setSimDevice(boolean isSimDevice) {
		this.isSimDevice = isSimDevice;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public Session[] getSessions() {
		return sessions;
	}

	public void setSessions(Session[] sessions) {
		this.sessions = sessions;
	}
}
