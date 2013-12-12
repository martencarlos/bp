package ceu.marten.services;

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
import android.util.Log;
import ceu.marten.bplux.R;
import ceu.marten.model.Configuration;
import ceu.marten.model.io.DataManager;
import ceu.marten.ui.NewRecordingActivity;

public class BiopluxService extends Service {

	private static final String TAG = BiopluxService.class.getName();

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_RECORDING_DURATION = 3;
	public static final int MSG_FIRST_DATA = 4;
	public static final int MSG_SECOND_DATA = 5;

	static Messenger client = null;
	private NotificationManager notificationManager;
	private Timer timer = new Timer();
	private boolean isWriting;
	private static DataManager dataManager;

	private String recordingName;
	private Configuration configuration;
	private int numberOfChannelsToDisplay;
	private ArrayList<Integer> channelsToDisplay;
	private ArrayList<Integer> activeChannels;

	private Device connection;
	private Device.Frame[] frames;

	private final Messenger mMessenger = new Messenger(new IncomingHandler());

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				client = msg.replyTo;
				break;
			case MSG_UNREGISTER_CLIENT:
				client = null;
				break;
			case MSG_RECORDING_DURATION:
				dataManager.setDuration(msg.getData().getString("duration"));
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public void onCreate() {
		frames = new Device.Frame[80];
		for (int i = 0; i < frames.length; i++)
			frames[i] = new Frame();
		Log.i(TAG, "Service created");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		getInfoFromActivity(intent);
		connectToBiopluxDevice();
		dataManager = new DataManager(this, activeChannels, recordingName,
				configuration);
		showNotification(intent);

		timer.schedule(new TimerTask() {
			public void run() {
				processFrames();
			}
		}, 0, 50L);

		return mMessenger.getBinder();
	}

	private void processFrames() {
		isWriting = true;
		getFrames(80);
		for (Frame f : frames) {
			dataManager.writeFramesToTmpFile(f);

		}
		isWriting = false;
		try {
			sendFirstGraphData(frames[0].an_in[(channelsToDisplay.get(0) - 1)]);
			if (numberOfChannelsToDisplay == 2)
				sendSecondGraphData(frames[0].an_in[(channelsToDisplay.get(1) - 1)]);

		} catch (Throwable t) {
			Log.e(TAG, "error processing frames", t);
		}
	}

	private void getFrames(int nFrames) {
		try {
			connection.GetFrames(nFrames, frames);
		} catch (BPException e) {
			Log.e(TAG, "exception getting frames", e);
		}
	}

	private void getInfoFromActivity(Intent intent) {
		recordingName = intent.getStringExtra("recordingName").toString();
		configuration = (Configuration) intent
				.getSerializableExtra("configSelected");
		activeChannels = configuration.getActiveChannels();
		numberOfChannelsToDisplay = configuration
				.getNumberOfChannelsToDisplay();
		channelsToDisplay = configuration.getChannelsToDisplay();
	}

	private void connectToBiopluxDevice() {

		// BIOPLUX INITIALIZATION
		try {
			connection = Device.Create(configuration.getMacAddress());
			// MAC EXAMPLE 00:07:80:4C:2A:FB
			connection.BeginAcq(configuration.getReceptionFrequency(),
					configuration.getActiveChannelsAsInteger(),
					configuration.getNumberOfBits());
		} catch (BPException e) {
			Log.e(TAG, "bioplux connection exception", e);
		}

	}

	private void showNotification(Intent parentIntent) {

		// SET THE BASICS
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.notification)
				.setContentTitle("Device Connected")
				.setContentText("service running, receiving data..");

		// EXTRA INFO ON INTENT
		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
		newRecordingIntent.putExtra("recordingName", recordingName);
		newRecordingIntent.putExtra("configSelected", configuration);

		// PENDING INTENT
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				newRecordingIntent, 0);
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setOngoing(true);

		// GET THE NOTIFICATION AND NOTIFY
		Notification notification = mBuilder.build();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(R.string.service_id, notification);
	}

	private void sendFirstGraphData(int intvaluetosend) {

		try {
			client.send(Message.obtain(null, MSG_FIRST_DATA, intvaluetosend, 0));
		} catch (RemoteException e) {
			Log.e(TAG, "client is dead. Removing from clients list", e);
			client = null;
		}

	}

	private void sendSecondGraphData(int intvaluetosend) {
		try {
			client.send(Message
					.obtain(null, MSG_SECOND_DATA, intvaluetosend, 0));
		} catch (RemoteException e) {
			Log.e(TAG, "client is dead. Removing from clients list", e);
			client = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY; // run until explicitly stopped.
	}

	@Override
	public void onDestroy() {

		if (timer != null)
			timer.cancel();

		while (isWriting) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e2) {
				Log.e(TAG, "Exception thread is sleeping", e2);
			}
		}
		dataManager.closeWriters();
		try {
			connection.EndAcq();
		} catch (BPException e) {
			Log.e(TAG, "error ending ACQ", e);
		}
		dataManager.saveFiles();
		notificationManager.cancel(R.string.service_id);
		Log.i(TAG, "service stopped");
		super.onDestroy();
	}
}
