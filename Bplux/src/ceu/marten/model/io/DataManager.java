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
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.j256.ormlite.dao.Dao;

import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.model.Recording;

import plux.android.bioplux.Device.Frame;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class DataManager {
	
	private static final String TAG = DataManager.class.getName();
	
	private String formatFileCollectedData = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";
	private OutputStreamWriter outStreamWriter;
	private BufferedWriter bufferedWriter;
	private short[] frameTmp;
	private int frameCounter;
	
	private Context context;
	private ArrayList<Integer> activeChannels;
	private String recordingName;
	private Configuration configuration;
	private String duration;
	
	
	public DataManager(Context serviceContext, ArrayList<Integer> _activeChannels,String _recordingName, Configuration _configuration) {
		this.context = serviceContext;
		this.activeChannels = _activeChannels;
		this.recordingName = _recordingName;
		this.configuration = _configuration;
		frameTmp = new short[8];
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
	

	public void writeFramesToTmpFile(Frame f) {
		frameCounter++;
		int index = 0;
		for (int i = 0; i < activeChannels.size(); i++) {
			index = activeChannels.get(i) - 1;
			frameTmp[index] = f.an_in[i];
		}
		try {
			bufferedWriter.write(String.format(formatFileCollectedData, frameCounter,
					String.valueOf(frameTmp[0]), String.valueOf(frameTmp[1]),
					String.valueOf(frameTmp[2]), String.valueOf(frameTmp[3]),
					String.valueOf(frameTmp[4]), String.valueOf(frameTmp[5]),
					String.valueOf(frameTmp[6]), String.valueOf(frameTmp[7])));
		} catch (IOException e) {
			Log.e(TAG, "Exception while writing frame row", e);
		}
		
	}
	
	private void compressFile() {
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
		}
		finally{
			try {
				origin.close();
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "Exception while closing streams", e);
			}
			
		}
	}
	
	private void writeTextFile() {

		DateFormat dateFormat = DateFormat.getDateTimeInstance();		
		Date date = new Date();
		OutputStreamWriter out = null;
		BufferedInputStream origin = null;
		BufferedOutputStream dest = null;
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
			out.write(String.format(formatFileCollectedData, "#num", "ch 1",
					"ch 2", "ch 3", "ch 4", "ch 5", "ch 6", "ch 7", "ch 8"));
			out.flush();
			out.close();

			// APPEND DATA
			FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
					+ "/" + recordingName + ".txt", true);
			dest = new BufferedOutputStream(outBytes);
			FileInputStream fi = new FileInputStream(context.getFilesDir() + "/"
					+ "tmp.txt");
			 
			origin = new BufferedInputStream(fi, 1000);
			int count;
			byte data[] = new byte[1000];
			while ((count = origin.read(data, 0, 1000)) != -1) {
				dest.write(data, 0, count);
			}

		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write header on, not found", e);
		} catch (IOException e) {
			Log.e(TAG, "write header stream exception", e);
		}
		finally{
			try {
				out.close();
				origin.close();
				dest.close();
				context.deleteFile("tmp.txt");
			} catch (IOException e) {
				Log.e(TAG, "closing streams exception", e);
			}
			
		}
	}
	
	public void closeWriters(){
		try {
			bufferedWriter.flush();
			bufferedWriter.close();
			outStreamWriter.close();
		} catch (IOException e1) {
			Log.e(TAG, "Exception while closing StreamWriter", e1);
		}
	}
	
	public void saveFiles(){
		writeTextFile();
		compressFile();
	}

	public void setDuration(String _duration) {
		this.duration = _duration;
		
	}

}
