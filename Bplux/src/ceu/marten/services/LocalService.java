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

public class LocalService extends Service {
	//@todo dar nombres a las cosas como Dios manda
	private NotificationManager notificationManager;
	private Timer timer = new Timer();
	private boolean sendingData = true;
	private static boolean isRunning = false;
	private Configuration config;
	private String recording_name;
	private int channelToDisplay = 0;
	
	//@todo esta es la forma estándar de hacer log en Android; actualiza todo el proyecto
	//mira la segunda parte de este comentario más abajo
	private static final String TAG = LocalService.class.getName();

	private Device connection;
	private Device.Frame[] frames;
	private int counter=0;

	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_START_SENDING_DATA = 3;
	public static final int MSG_STOP_SERVICE = 4;
	public static final int MSG_VALUE = 5;
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
				// sendingData = true;
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

		//@todo esta es la segunda parte del comentario
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
		//@todo define estas cadenas de caracteres como variables globales de la clase para qué no haya que
		//reservar las cada vez que se meta dentro de este método
		String formatHeader = "%-10s %-10s%n";
		String formatStr = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";
		try {
			out = new OutputStreamWriter(openFileOutput(recording_name+".txt",MODE_PRIVATE ));
			out.write(String.format(formatHeader,"configuration name: ",config.getName())); 
			out.write(String.format(formatHeader,"freq: ",config.getFrequency()));
			out.write(String.format(formatHeader,"nbits: ",config.getNumberOfBits()));
			out.write(String.format(formatHeader,"start date and time ",config.getCreateDate()));
			out.write(String.format(formatHeader,"channels active: ",config.getActiveChannelsAsString()));
			out.write(String.format(formatStr, "#num","ch 1", 
					"ch 2", "ch 3", "ch 4", "ch 5",
					"ch 6", "ch 7", "ch 8"));
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();//@todo no imprimas ninguna traza ¡haz log! no quiero ver un solo printStackTrace() en el proyecto
		} catch (IOException e) {
			e.printStackTrace();
		}//@todo ¿y cerrar los ficheros?
	}
	public void writeFrameToTextFile(Frame f){
		//@todo ¿es la misma cadena de caracteres que en la clase anterior? No repetirse!
		String formatStr = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";
		counter++;
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(openFileOutput(recording_name+".txt", MODE_APPEND));
			out.write(String.format(formatStr, counter,String.valueOf(f.an_in[0]), 
					String.valueOf(f.an_in[1]), String.valueOf(f.an_in[2]), String.valueOf(f.an_in[3]), String.valueOf(f.an_in[4]),
					String.valueOf(f.an_in[5]), String.valueOf(f.an_in[6]), String.valueOf(f.an_in[7])));
			out.close();
		} catch (FileNotFoundException e) {
						e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}//@todo ¿y cerrar los ficheros?
		
	}
	private void readFile() {
		InputStream in = null;
		try {
			in = openFileInput(recording_name+".txt");
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
		
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}// @todo ¿y cerrar los ficheros?
	}

	private void getInfoFromActivity(Intent intent) {
		recording_name = intent.getStringExtra("recordingName").toString();
		config = (Configuration) intent.getSerializableExtra("configSelected");
//@todo ¡no crees objetos a lo tonto! ¡Y dale nombres Java!
		boolean[] ctdTmp;
		ctdTmp = config.getChannelsToDisplay();
		for (int i = 0; i < ctdTmp.length; i++)
			if (ctdTmp[i]) {
				channelToDisplay = (i + 1);
				Log.d("test", "channel to display: " + channelToDisplay);
			}

	}

	private void connectToBiopluxDevice() {

		// bioPlux initialization
		try {
			connection = Device.Create(config.getMacAddress());// Device mac
																// addr
																// 00:07:80:4C:2A:FB
			//@todo entiendo que falta configurar los canales que se adquieren realmente ¿no?
			connection.BeginAcq(config.getFrequency(), 255, config.getNumberOfBits());
		} catch (BPException e) {
			e.printStackTrace();
		}

	}

	private void processFrames() {
		Log.d("test", "doing work");
		try {
			getFrames(20);
			for (Frame f : frames) {
				sendMessageToUI(f.an_in[(channelToDisplay - 1)]);
				writeFrameToTextFile(f);
			}
		} catch (Throwable t) {
			Log.d("test", "TIMER ERROR", t);
		}
	}

	private void getFrames(int nFrames) {
		try {
			connection.GetFrames(nFrames, frames);
		} catch (BPException e) {
			e.printStackTrace();
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
		newRecActIntent.putExtra("recordingName", recording_name);
		newRecActIntent.putExtra("configSelected", config);
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
				// Send data as an Integer
				mClients.get(i).send(
						Message.obtain(null, MSG_VALUE, intvaluetosend, 0));
				/*
				 * //Send data as a String Bundle b = new Bundle();
				 * b.putString("str1", "ab" + intvaluetosend + "cd"); Message
				 * msg = Message.obtain(null, MSG_SET_STRING_VALUE);
				 * msg.setData(b); mClients.get(i).send(msg);
				 */
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("bplux_service", "Received start id " + startId + ": " + intent);
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
		// writer.closeWriter();
		try {
			connection.EndAcq();
		} catch (BPException e) {
			Log.d("test", "error ending ACQ");
			e.printStackTrace();
		}
		notificationManager.cancel(R.string.service_id); // Cancel the persistent notification.
		Log.d("MyService", "Service Stopped.");
		isRunning = false;
	}
}
