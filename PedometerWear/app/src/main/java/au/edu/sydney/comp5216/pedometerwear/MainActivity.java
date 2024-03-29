package au.edu.sydney.comp5216.pedometerwear;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends WearableActivity {
    private long timestamp;
    private Thread detectorTimeStampUpdaterThread;
    private TextView tx_steps;
    private TextView tx_time;
    private Handler handler;
    private boolean isRunning = true;
    private float steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tx_steps = (TextView) findViewById(R.id.tx_steps);
        tx_time = (TextView) findViewById(R.id.tx_time);
        registerForSensorEvents();
        steps = 0;
        setupDetectorTimestampUpdaterThread();
        // Enables Always-on
        setAmbientEnabled();
    }

    public void registerForSensorEvents() {
        SensorManager sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Step Counter
        sManager.registerListener(new SensorEventListener() {
                                      @Override
                                      public void onSensorChanged(SensorEvent event) {

                                          // steps += event.values[0];
                                          tx_steps.setText((int) steps + "");
                                          steps += 1;
                                      }

                                      @Override
                                      public void onAccuracyChanged(Sensor sensor, int accuracy) {
                                      }
                                  }, sManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI);
        // Step Detector
        sManager.registerListener(new SensorEventListener() {
                                      @Override
                                      public void onSensorChanged(SensorEvent event) {
                                          // Set the time when there is new sensor data
                                          timestamp = System.currentTimeMillis()
                                                  + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
                                      }

                                      @Override
                                      public void onAccuracyChanged(Sensor sensor, int accuracy) {
                                      }
                                  }, sManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_UI);
    }

    private void setupDetectorTimestampUpdaterThread() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                tx_time.setText(DateUtils.getRelativeTimeSpanString(timestamp));
            }
        };
        detectorTimeStampUpdaterThread = new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(5000);
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        detectorTimeStampUpdaterThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        detectorTimeStampUpdaterThread.interrupt();
    }

    public void onClean(View v) {
        steps = 0;
        tx_steps.setText("0");

    }
}
