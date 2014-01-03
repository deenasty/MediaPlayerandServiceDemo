
package com.issamux;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MyService extends Service implements MediaPlayer.OnPreparedListener {

	private static final String ACTION_PLAY = "com.issamux.action.PLAY";
	private static final String ACTION_STOP = "com.issamux.action.STOP";
	private static final int NOTIFICATION_ID = 11;
	private static boolean isRunning = false;
	private static MediaPlayer mediaPlayer = new MediaPlayer();

	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, " bind on service", Toast.LENGTH_LONG).show();
		return new Binder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Toast.makeText(this, " Unbind from service", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "starting service", Toast.LENGTH_LONG).show();
		isRunning = true;
		if (intent != null && intent.getAction() != null) {

			if (intent.getAction().equals(ACTION_PLAY)) {
				Toast.makeText(this, "playing sound", Toast.LENGTH_LONG).show();
				playMusic();
			}
			if (intent.getAction().equals(ACTION_STOP)) {
				Toast.makeText(this, "stop playing sound", Toast.LENGTH_LONG).show();
				stopMusic();
			}
		}
		return START_NOT_STICKY;
	}

	private void playMusic() {
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound_file_1);
		mediaPlayer.setOnPreparedListener(this);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // prepare async to not block main thread
	}

	private void prepareNotif() {
		// assign the song name to songName
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
				getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = "playing..." + getTimeString(mediaPlayer.getDuration());
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFICATION_ID, notification);
	}

	private String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();

		int hours = (int) (millis / (1000 * 60 * 60));
		int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
		int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

		buf.append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes))
				.append(":").append(String.format("%02d", seconds));

		return buf.toString();
	}

	public void stopMusic() {
		stopForeground(true);
		mediaPlayer.release();
		mediaPlayer = null;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	public static boolean isPlaying() {
		if (mediaPlayer != null)
			return mediaPlayer.isPlaying();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();
		prepareNotif();
	}

}
