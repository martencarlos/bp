package ceu.marten.bplux;

import java.io.Serializable;

import plux.android.bioplux.BPException;
import plux.android.bioplux.Device;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by martencarlos on 25/07/13.
 */
public class BPDevice implements Serializable{


	private static final long serialVersionUID = 1L;
	Device device = null;
    String name = null;
    Device.Frame[] frames = null;
    String description = null;
    float freq = 0;
    int channel = 0;
    int nBits = 0; //number of bits can be 8 or 12 [0-255] | [0-4095]
    boolean digOutput = false;
    boolean isSimDevice = false;
    

    public BPDevice() {
    	/*
        frames = new Device.Frame[1];

        //initialize frames array
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Device.Frame();
        }

        //bioPlux initialization
        try {
            device = Device.Create("test");//Device mac addr 00:07:80:4C:2A:FB
        } catch (BPException e) {
            e.printStackTrace();
            Log.d("BPexception", e.getMessage());
        }
        */

    }
    
	@SuppressWarnings("unused")
	private double getFrame(int n) {
        try {
            device.GetFrames(1, frames);
        } catch (BPException e) {
            e.printStackTrace();
        }

        return (double) frames[0].an_in[n];
    }
	
	public void beginAcq(){
		try {
			device.BeginAcq();
		} catch (BPException e) {
			e.printStackTrace();
		}
	}

	
    //GETTERS AND SETTERS

    public Device.Frame[] getFrames() {
        return frames;
    }

    public void setFrames(Device.Frame[] frames) {
        this.frames = frames;
    }

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
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
	
	
    
    
}
