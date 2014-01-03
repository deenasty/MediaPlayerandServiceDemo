
package com.issamux;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private Context context;
	private MyService mService;
	private Button startServiceBtn, stopServiceBtn;
	private ToggleButton playSound;
	private TextView elabsedTime;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("MainActivity", "onServiceDisconnected");

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("MainActivity", "onServiceConnected");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		initUI();
	}

	private void initUI() {
		elabsedTime = (TextView) findViewById(R.id.textView1);
		playSound = (ToggleButton) findViewById(R.id.play_sound);
		startServiceBtn = (Button) findViewById(R.id.start_service_btn);
		stopServiceBtn = (Button) findViewById(R.id.stop_service_btn);
		playSound.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MyService.class);

				if (!MyService.isPlaying())
					intent.setAction("com.issamux.action.PLAY");
				else
					intent.setAction("com.issamux.action.STOP");

				startService(intent);

			}
		});

		startServiceBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MyService.class);
				if (MyService.isRunning()) {
					bindService(intent, connection, Context.BIND_AUTO_CREATE);
				}
				else {
					startService(intent);
					bindService(intent, connection, Context.BIND_AUTO_CREATE);
				}
			}
		});
		stopServiceBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (connection != null)
					unbindService(connection);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void checkIfServiceisRunning() {
		Intent intent = new Intent(this, MyService.class);
		if (MyService.isRunning()) {
			bindService(intent, connection, Context.BIND_AUTO_CREATE);
		}
		else {
			startService(intent);
			bindService(intent, connection, Context.BIND_AUTO_CREATE);
		}
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
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	public void setMyAlarm() {
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, MyService.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 60 * 1000, alarmIntent);
	}
}
