package ceu.marten.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import ceu.marten.data.Configuration;

import plux.android.bioplux.Device.Frame;

public class FileWriterIO {
	

	private Configuration config;
	private String fileName;

	public FileWriterIO(Configuration config, String recordingName) {
		super();
		this.config = config;
		this.fileName = recordingName;
	}

	public void writeHeaderOfTextFile() throws IOException {
		
		
	    
	
		/*
		file = new File(fileName+".txt");
		
		try {
			 writer = new FileWriter(file ,true);
			 writer.write(config.getName());
			 writer.write(System.getProperty("line.separator"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}


	public void writeFrameToTextFile(Frame f){
		String formatStr = "%-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n";
		
		
		/*
		try {
			writer.write(String.format(formatStr, String.valueOf(f.an_in[0]), 
					String.valueOf(f.an_in[1]), String.valueOf(f.an_in[2]), String.valueOf(f.an_in[3]), String.valueOf(f.an_in[4]),
					String.valueOf(f.an_in[5]), String.valueOf(f.an_in[6]), String.valueOf(f.an_in[7])));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	
	
}