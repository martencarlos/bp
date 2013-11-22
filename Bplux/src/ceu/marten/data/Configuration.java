package ceu.marten.data;

import java.io.Serializable;

import android.util.Log;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martencarlos on 25/07/13.
 */

@DatabaseTable(tableName = "deviceConfigs")
public class Configuration implements Serializable {
	
	private static final long serialVersionUID = -4487071327586521666L;
	@DatabaseField(generatedId = true)
	private Integer id;
	
	@DatabaseField(canBeNull = true)
	private String name = null;
	@DatabaseField(canBeNull = true)
	private String mac_address = null;
	@DatabaseField(canBeNull = true)
	private String createDate = null;
	@DatabaseField(canBeNull = true)
	private int freq = 0;
	@DatabaseField(canBeNull = true)
	private int nBits = 8; //number of bits can be 8 or 12 [0-255] | [0-4095]
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] activeChannels = null;
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] channelsToDisplay = null;

	
	public Configuration() {
		//needed for the OrmLite to generate object when query invoked
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String getMac_address() {
		return mac_address;
	}


	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}


	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	
	public int getFreq() {
		return freq;
	}


	public int getnBits() {
		return nBits;
	}


	public void setnBits(int nBits) {
		this.nBits = nBits;
	}

	public void setchannelsToDisplay(boolean[] boo) {
		int iterator=0;
		String[] channelsToDisplay= new String[8];
		for(boolean b:boo){
			if(b)
				channelsToDisplay[iterator]="true";
			else
				channelsToDisplay[iterator]="false";
			iterator++;
		}
			
		StringBuilder sb = new StringBuilder();
        for (int i=0; i<channelsToDisplay.length; i++) {
            sb.append(channelsToDisplay[i]);
            if (i != channelsToDisplay.length-1) {
                sb.append("*.*"); //concatenate by this splitter
            }
        }
		this.channelsToDisplay = sb.toString().getBytes();
	}


	public boolean[] getchannelsToDisplay() {
		 String entire = new String(this.channelsToDisplay);
		 String[] channelsToDisplay=  entire.split("\\*\\.\\*");
		 int iterator=0;
			boolean[] result= new boolean[8];
			for(String s:channelsToDisplay){
				if(s.equalsIgnoreCase("true"))
					result[iterator]=true;
				else
					result[iterator]=false;
				iterator++;
			}
	     return result;
	}


	public void setActiveChannels(String[] activeChannels) {
		StringBuilder sb = new StringBuilder();
        for (int i=0; i<activeChannels.length; i++) {
            sb.append(activeChannels[i]);
            if (i != activeChannels.length-1) {
                sb.append("*.*"); //concatenate by this splitter
            }
        }
		this.activeChannels = sb.toString().getBytes();
	}
	
	public String[] getActiveChannels() {
		 String entire = new String(this.activeChannels);
	     return entire.split("\\*\\.\\*");
	}
	private String activeChannelsToString(){
		String strActChnls="";
		
		return strActChnls;
	}
	
	@Override
	public String toString() {
		return "name "+ name + "; " + "freq "+freq+"; "+ "nBits "+nBits+"; "+
				"\n Active channels ";//+activeChannelsToString()+"\n";
	}
}
/*
	public BPDevice(String address){
		
		  frames = new Device.Frame[1];
		  
		  //initialize frames array 
		  for (int i = 0; i < frames.length; i++) {
			  frames[i] = new Device.Frame(); }
		  
		  //bioPlux initialization 
		  try { 
			  Log.d("devices", "entra: ");
			  connection = Device.Create("test");//Device mac addr 00:07:80:4C:2A:FB
		     Log.d("devices", "connection: "+connection.toString());
		  } catch(BPException e) { 
			  e.printStackTrace(); 
			  Log.d("BPexception", e.getMessage()); }
	}

	public double getFrame(int channel) {
		try {
			connection.GetFrames(1, frames);
		} catch (BPException e) {
			e.printStackTrace();
		}

		return (double) frames[0].an_in[channel];
	}

	public void beginAcq() {
		try {
			connection.BeginAcq();
		} catch (BPException e) {
			e.printStackTrace();
		}
	}
*/
	// GETTERS AND SETTERS
