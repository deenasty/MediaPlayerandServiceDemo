/*
 * @author issamux
 */

package com.issamux;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity2 extends Activity implements OnClickListener,
		MediaPlayer.OnPreparedListener {

	private MediaPlayer mediaPlayer;
	private TextView totalDuration, durationCounter;
	private Button startBtn, pauseBtn, stopBtn;
	private SeekBar seekBarProgress;
	private Context context;
	private boolean isPaused;

	private ProgressAsyncTask progressTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_2);
		context = this;
		initMediaPlayer();
		initUI();
	}

	private void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();
	}

	private void initUI() {
		startBtn = (Button) findViewById(R.id.play_button);
		pauseBtn = (Button) findViewById(R.id.pause_button);
		stopBtn = (Button) findViewById(R.id.stopBtm);
		totalDuration = (TextView) findViewById(R.id.totalDuration);
		durationCounter = (TextView) findViewById(R.id.durationCounter);
		seekBarProgress = (SeekBar) findViewById(R.id.seekBar1);
		// set listener
		totalDuration.setOnClickListener(this);
		durationCounter.setOnClickListener(this);
		startBtn.setOnClickListener(this);
		pauseBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);
		seekBarProgress.setOnClickListener(this);

	}

	private void prepareMPlayer() {
		mediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1);
		mediaPlayer.setOnPreparedListener(this);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			Log.i("Main", "IllegalStateException: " + e.getMessage());
		} catch (IOException e) {
			Log.i("Main", "IOException: " + e.getMessage());
		}
	}

	public void stopMusic() {
		mediaPlayer.release();
		mediaPlayer = null;
		progressTask.cancel(true);
		progressTask = null;
	}

	private void resumePlay() {
		mediaPlayer.start();
		isPaused = false;
		progressTask.resume();

	}

	private void pausePlay() {
		isPaused = true;
		mediaPlayer.pause();
		progressTask.suspend();

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

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.play_button) {
			if (mediaPlayer.isPlaying())
				return;
			prepareMPlayer();
		}
		else if (v.getId() == R.id.stopBtm) {
			Toast.makeText(this, "stopBtm", Toast.LENGTH_LONG).show();
			mediaPlayer.reset();
			seekBarProgress.setMax(0);
		}
		else {
			if (v.getId() == R.id.pause_button) {

				if (isPaused) {
					resumePlay();
					((TextView) v).setText("pause");
				}
				else {
					((TextView) v).setText("resume");
					pausePlay();
				}

			}
		}

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();
		totalDuration.setText(getTimeString(mediaPlayer.getDuration()));
		progressTask = new ProgressAsyncTask();
		progressTask.execute();

	}

	class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

		boolean isSuspended;

		public ProgressAsyncTask() {
			isSuspended = false;
		}

		void suspend() {
			isSuspended = true;
		}

		synchronized void resume() {
			isSuspended = false;
			notify();
		}

		@Override
		protected Void doInBackground(Void... params) {
			seekBarProgress.setMax(mediaPlayer.getDuration());
			while (mediaPlayer != null && mediaPlayer.isPlaying()) {
				seekBarProgress.setProgress(mediaPlayer.getCurrentPosition());
				// publish media player progress
				publishProgress(mediaPlayer.getCurrentPosition());
				// case we pause thread
				synchronized (this) {
					while (isSuspended) {
						try {
							wait();
						} catch (InterruptedException e) {
							Log.i("MyThread", "InterruptedException" + e.getMessage());
						}
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// update UI
			durationCounter.setText(getTimeString(progress[0]));
		}

	}

}