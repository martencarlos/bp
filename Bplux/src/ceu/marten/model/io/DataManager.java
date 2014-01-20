package ceu.marten.model.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ceu.marten.bplux.R;
import ceu.marten.model.DeviceConfiguration;

import plux.android.bioplux.Device.Frame;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class DataManager {
	
	private static final String TAG = DataManager.class.getName();
	
	private String channelFormat = "%-4s ";
	private OutputStreamWriter outStreamWriter;
	private BufferedWriter bufferedWriter;
	private int frameCounter;
	
	private Context context;
	private String recordingName;
	private DeviceConfiguration configuration;
	private int numberOfChannelsActivated;
	private String duration;
	
	
	public DataManager(Context serviceContext,String _recordingName, DeviceConfiguration _configuration) {
		this.context = serviceContext;
		this.recordingName = _recordingName;
		this.configuration = _configuration;
		this.numberOfChannelsActivated = configuration.getNumberOfChannelsActivated();
		frameCounter = 0;
		
		try {
			outStreamWriter = new OutputStreamWriter(context.openFileOutput(
					"tmp.txt", Context.MODE_APPEND));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write frames on, not found", e);
		}
		bufferedWriter = new BufferedWriter(outStreamWriter);
	}
	
	public DataManager(Context context){
		this.context = context;  
	}
	
	//WRITES A ROW ON THE TEXT FILE THAT WILL GO AFTER THE HEADER
	public boolean writeFramesToTmpFile(Frame f) {
		frameCounter++;

		try {
			//WRITE THE FIRST COLUMN (THE FRAME COUNTER)
			bufferedWriter.write(String.format(channelFormat, frameCounter));
			//WRITE THE DATA OF ACTIVE CHANNELS ONLY
			for(int i=0; i< numberOfChannelsActivated;i++){
				bufferedWriter.write(String.format(channelFormat, f.an_in[i]));
			}
			//WRITE A NEW LINE
			bufferedWriter.write("\n");
			
		} catch (IOException e) {
			Log.e(TAG, "Exception while writing frame row", e);
			try {
				bufferedWriter.close();
			} catch (IOException e1) {
				Log.e(TAG, "Exception while closing bufferedWriter", e1);
			}
			return false;
		}
		return true;
		
	}
	
	private boolean compressFile(){
		BufferedInputStream origin = null;
		ZipOutputStream out = null;
		try {
			String zipFileName = recordingName + ".zip";
			String file = recordingName + ".txt";
			String appDirectory = Environment.getExternalStorageDirectory().toString()+"/Bioplux/";
			File root = new File(appDirectory);
			root.mkdirs();
			int BUFFER = 500;
			
			FileOutputStream dest = new FileOutputStream(root +"/"+ zipFileName);
					
			out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte data[] = new byte[BUFFER];

			FileInputStream fi = new FileInputStream(context.getFilesDir() + "/" + file);
			origin = new BufferedInputStream(fi, BUFFER);

			ZipEntry entry = new ZipEntry(
					file.substring(file.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
			int count;

			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			context.deleteFile(recordingName + ".txt");

		} catch (Exception e) {
			Log.e(TAG, "exception while zipping", e);
			return false;
		}
		finally{
			try {
				origin.close();
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "Exception while closing streams", e);
				return false;
			}	
		}
		return true;
	}
	
	private boolean writeTextFile() {

		DateFormat dateFormat = DateFormat.getDateTimeInstance();		
		Date date = new Date();
		OutputStreamWriter out = null;
		BufferedInputStream origin = null;
		BufferedOutputStream dest = null;
		FileInputStream fi = null;
		try {
			out = new OutputStreamWriter(context.openFileOutput(
					recordingName + ".txt", Context.MODE_PRIVATE));
			out.write(String.format("%-10s %-10s%n", "# "+context.getString(R.string.bs_header_name),
					configuration.getName()));
			out.write(String.format("%-10s %-14s%n", "# "+context.getString(R.string.bs_header_date),
					dateFormat.format(date)));
			out.write(String.format("%-10s %-4s%n", "# "+context.getString(R.string.bs_header_frequency),
					configuration.getReceptionFrequency() + " Hz"));
			out.write(String.format("%-10s %-10s%n", "# "+context.getString(R.string.bs_header_bits),
					configuration.getNumberOfBits() + " bits"));
			out.write(String.format("%-10s %-14s%n", "# "+context.getString(R.string.bs_header_duration), duration
					+ " seconds"));
			out.write(String.format("%-10s %-14s%n%n", "# "+context.getString(R.string.bs_header_active_channels),
					configuration.getActiveChannelsAsString()));
			
			out.write("#num ");
			for(int i: configuration.getActiveChannels())
				out.write("ch "+i+" ");
			out.write("\n");
			out.flush();
			out.close();

			// APPEND DATA
			FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
					+ "/" + recordingName + ".txt", true);
			dest = new BufferedOutputStream(outBytes);
			fi = new FileInputStream(context.getFilesDir() + "/"
					+ "tmp.txt");
			 
			origin = new BufferedInputStream(fi, 1000);
			int count;
			byte data[] = new byte[1000];
			while ((count = origin.read(data, 0, 1000)) != -1) {
				dest.write(data, 0, count);
			}

		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write header on, not found", e);
			return false;
		} catch (IOException e) {
			Log.e(TAG, "write header stream exception", e);
			return false;
		}
		finally{
			try {
				fi.close();
				out.close();
				origin.close();
				dest.close();
				context.deleteFile("tmp.txt");
			} catch (IOException e) {
				Log.e(TAG, "closing streams exception", e);
				return false;
			}
		}
		return true;
	}
	
	public boolean closeWriters(){
		try {
			bufferedWriter.flush();
			bufferedWriter.close();
			outStreamWriter.close();
		} catch (IOException e1) {
			Log.e(TAG, "Exception while closing StreamWriter", e1);
			return false;
		}
		return true;
	}
	
	public boolean saveFiles(){
		if(!writeTextFile())
			return false;
		if(!compressFile())
			return false;
		return true;
	}

	public void setDuration(String _duration) {
		this.duration = _duration;
		
	}

}
