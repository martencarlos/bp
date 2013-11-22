package ceu.marten.services;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
import ceu.marten.activities.NewRecordingActivity;
import ceu.marten.activities.RecordingConfigsActivity;
import ceu.marten.bplux.R;

public class LocalService extends Service {
    private NotificationManager nm;
    private Timer timer = new Timer();
    private int counter = 0;
    private boolean sendingData = false;
    private static boolean isRunning = false;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_START_SENDING_DATA = 3;
    public static final int MSG_STOP_SENDING_DATA = 4;
    public static final int MSG_VALUE = 5;
    public static final int MSG_SET_STRING_VALUE = 6;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.


    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case MSG_REGISTER_CLIENT:
	            mClients.add(msg.replyTo);
	            break;
	        case MSG_UNREGISTER_CLIENT:
	            mClients.remove(msg.replyTo);
	            stopSelf();
	            break;
	        case MSG_START_SENDING_DATA:
	            sendingData = true;
	            break;
	        case MSG_STOP_SENDING_DATA:
	            sendingData = false;
	            break;
	        default:
	            super.handleMessage(msg);
	        }
	    }
	}
    
	@Override
	public void onCreate() {
	    super.onCreate();
	    Log.d("bplux_service", "Service Started.");
	    showNotification();
	    timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 0, 100L);
	    isRunning = true;
	}

	private void showNotification() {
		
		//SET THE BASICS
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Device Connected")
		        .setContentText("service running, receiving data..");
	
		//SET BACK BUTTON PROPERLY
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(RecordingConfigsActivity.class);
		Intent newRecActIntent = new Intent(this, NewRecordingActivity.class);
		stackBuilder.addNextIntent(newRecActIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		
		
		//ADDING AN ACTION
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, newRecActIntent, 0);
		mBuilder.addAction(R.drawable.ic_undo, "stop service", pIntent);
		mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(true);
		Notification notification = mBuilder.build();
		
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    nm.notify(R.string.service_id, notification);
	}

	@Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
	
    private void onTimerTick() {
	    Log.d("bplux_service", "Timer doing work." + counter);
	    try {
	        counter += 1;
	        if(sendingData)
	        	sendMessageToUI(counter);
	       
	    } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
	        Log.d("bplux_service", "Timer Tick Failed.", t);            
	    }
	}

	private void sendMessageToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_VALUE, intvaluetosend, 0));
                /*
                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);
                */
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("bplux_service", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning()
    {
        return isRunning;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {timer.cancel();}
        counter=0;
        nm.cancel(R.string.service_id); // Cancel the persistent notification.
        Log.d("MyService", "Service Stopped.");
        isRunning = false;
    }
}
