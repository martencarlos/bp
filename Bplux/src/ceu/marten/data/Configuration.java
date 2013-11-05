package ceu.marten.data;

import java.io.Serializable;

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
	private String description = null;
	@DatabaseField(canBeNull = true)
	private String createDate = null;
	@DatabaseField(canBeNull = true)
	private float freq = 0;
	@DatabaseField(canBeNull = true)
	private int nBits = 0; //number of bits can be 8 or 12 [0-255] | [0-4095]
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] activeChannels = null;

	
	public Configuration() {
		//needed for the OrmLite to generate object when query invoked
	}
	
	public Configuration(String initName, String initDescription, float initFreq, int initNbits, byte[] initActiveChannels){
		this.name = initName;
		this.description = initDescription;
		this.freq = initFreq;
		this.nBits = initNbits;
		this.activeChannels = initActiveChannels;
		
		
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setFreq(float freq) {
		this.freq = freq;
	}

	public void setnBits(int nBits) {
		this.nBits = nBits;
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
		String[] myChannels = this.getActiveChannels();
		for(int i=0;i<myChannels.length;i++){
			if(myChannels[i].compareTo("activo") == 0)
				strActChnls = strActChnls + "channel "+(i+1)+", ";
		}
		return strActChnls;
		
	}
	
	@Override
	public String toString() {
		return "name "+ name + "; " + "freq "+freq+"; "+ "nBits "+nBits+"; "+
				"\n Active channels "+activeChannelsToString()+"\n";
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
