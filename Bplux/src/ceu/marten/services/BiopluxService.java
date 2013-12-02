package ceu.marten.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import plux.android.bioplux.BPException;
import plux.android.bioplux.Device;
import plux.android.bioplux.Device.Frame;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.ui.NewRecordingActivity;
import ceu.marten.ui.RecordingConfigsActivity;

public class BiopluxService extends Service {

	private static final String TAG = BiopluxService.class.getName();

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_START_SENDING_DATA = 3;
	public static final int MSG_STOP_SERVICE = 4;
	public static final int MSG_VALUE = 5;

	private String formatFileHeader = "%-10s %-10s%n";
	private String formatFileCollectedData = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";

	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	private NotificationManager notificationManager;
	private Timer timer = new Timer();
	private static boolean isRunning = false;
	private Configuration configuration;
	private String recordingName;
	private int channelToDisplay = 0;

	private Device connection;
	private Device.Frame[] frames;
	private int counter = 0;

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler { // Handler of incoming messages from
											// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				readFile();
				break;
			case MSG_START_SENDING_DATA:
				break;
			case MSG_STOP_SERVICE:
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		frames = new Device.Frame[20];
		for (int i = 0; i < frames.length; i++)
			frames[i] = new Frame();

		Log.d(TAG, "Service created");
	}

	@Override
	public IBinder onBind(Intent intent) {
		getInfoFromActivity(intent);
		connectToBiopluxDevice();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {

				processFrames();
			}
		}, 0, 100L);
		isRunning = true;
		showNotification(intent);
		writeHeaderOfTextFile();
		return mMessenger.getBinder();
	}

	private void writeHeaderOfTextFile() {
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(openFileOutput(recordingName + ".txt",
					MODE_PRIVATE));
			out.write(String.format(formatFileHeader, "configuration name: ",
					configuration.getName()));
			out.write(String.format(formatFileHeader, "freq: ",
					configuration.getFrequency()));
			out.write(String.format(formatFileHeader, "nbits: ",
					configuration.getNumberOfBits()));
			out.write(String.format(formatFileHeader, "start date and time ",
					configuration.getCreateDate()));
			out.write(String.format(formatFileHeader, "channels active: ",
					configuration.getActiveChannelsAsString()));
			out.write(String.format(formatFileCollectedData, "#num", "ch 1",
					"ch 2", "ch 3", "ch 4", "ch 5", "ch 6", "ch 7", "ch 8"));
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write header on, not found",e);
		} catch (IOException e) {
			Log.e(TAG, "write header stream exception",e);
		}
	}

	public void writeFrameToTextFile(Frame f) {
		counter++;
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(openFileOutput(recordingName + ".txt",
					MODE_APPEND));
			out.write(String.format(formatFileCollectedData, counter,
					String.valueOf(f.an_in[0]), String.valueOf(f.an_in[1]),
					String.valueOf(f.an_in[2]), String.valueOf(f.an_in[3]),
					String.valueOf(f.an_in[4]), String.valueOf(f.an_in[5]),
					String.valueOf(f.an_in[6]), String.valueOf(f.an_in[7])));
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write frames on, not found",e);
		} catch (IOException e) {
			Log.e(TAG, "write frames stream exception",e);
		}// @todo ¿y cerrar los ficheros?

	}

	private void readFile() {
		InputStream in = null;
		try {
			in = openFileInput(recordingName + ".txt");
			if (in != null) {
				InputStreamReader tmp = new InputStreamReader(in);
				BufferedReader reader = new BufferedReader(tmp);
				String str;
				StringBuilder buf = new StringBuilder();
				while ((str = reader.readLine()) != null) {
					buf.append(str + "\n");
				}
				in.close();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to read, not found",e);
		} catch (IOException e) {
			Log.e(TAG, "file to read, stream expception",e);
		}// @todo ¿y cerrar los ficheros?
	}

	private void getInfoFromActivity(Intent intent) {
		recordingName = intent.getStringExtra("recordingName").toString();
		configuration = (Configuration) intent
				.getSerializableExtra("configSelected");
		
		boolean[] channelsToDisplayTmp;
		channelsToDisplayTmp = configuration.getChannelsToDisplay();
		for (int i = 0; i < channelsToDisplayTmp.length; i++)
			if (channelsToDisplayTmp[i]) {
				channelToDisplay = (i + 1);
			}
	}

	private void connectToBiopluxDevice() {

		// bioPlux initialization
		try {
			connection = Device.Create(configuration.getMacAddress());
			// Device mac addr 00:07:80:4C:2A:FB
			// TODO still need to be implemented
			connection.BeginAcq(configuration.getFrequency(), 255,
					configuration.getNumberOfBits());
		} catch (BPException e) {
			Log.e(TAG, "bioplux connection exception",e);
		}

	}

	private void processFrames() {
		Log.d(TAG, "service doing work");
		try {
			getFrames(20);
			for (Frame f : frames) {
				sendMessageToUI(f.an_in[(channelToDisplay - 1)]);
				writeFrameToTextFile(f);
			}
		} catch (Throwable t) {
			Log.e(TAG, "error processing frames",t);
		}
	}

	private void getFrames(int nFrames) {
		try {
			connection.GetFrames(nFrames, frames);
		} catch (BPException e) {
			Log.e(TAG, "exception getting frames",e);
		}
	}

	private void showNotification(Intent parentIntent) {

		// SET THE BASICS
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Device Connected")
				.setContentText("service running, receiving data..");

		// SET BACK BUTTON PROPERLY
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(RecordingConfigsActivity.class);
		Intent newRecActIntent = new Intent(this, NewRecordingActivity.class);
		newRecActIntent.putExtra("recordingName", recordingName);
		newRecActIntent.putExtra("configSelected", configuration);
		newRecActIntent.putExtra("notification", true);
		stackBuilder.addNextIntent(newRecActIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		// mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(true);
		Notification notification = mBuilder.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(R.string.service_id, notification);
	}

	private void sendMessageToUI(int intvaluetosend) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				mClients.get(i).send(
						Message.obtain(null, MSG_VALUE, intvaluetosend, 0));
			} catch (RemoteException e) {
				Log.e(TAG, "client is dead. Removing from clients list",e);
				mClients.remove(i);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY; // run until explicitly stopped.
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (timer != null) {
			timer.cancel();
		}
		try {
			connection.EndAcq();
		} catch (BPException e) {
			Log.e(TAG, "error ending ACQ", e);
		}
		notificationManager.cancel(R.string.service_id); 
		Log.d(TAG, "service stopped");
		isRunning = false;
	}
}
