package com.seventhmoon.sensortest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mGyroscope;
    private Sensor mGyroscope_uncalibrated;
    private Sensor mLinearAcceration;
    private Sensor mRotationVector;
    private Sensor mStepCounter;
    private SensorEventListener accelerometerListener;

    private static double PI = 3.1415926535897932384626433832795;
    private static double gravity = 9806.65;

    private static long previous_time = 0;
    private static long current_time = 0;
    private static double previous_accel = 0.0;
    private static double current_accel = 0.0;

    private static double x_coordinate = 0.0;
    private static double y_coordinate = 0.0;

    private ListView listView;
    private SimpleAdapter simpleAdapter;

    private static List<Map<String, String>> myList = new ArrayList<>();
    private static String logMsg;
    private static boolean is_stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        Button btnStop = (Button) findViewById(R.id.btnStopOrPlay);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            Log.e(TAG, "Has mAccelerometer sensor!");
        }

        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (mGravity != null) {
            Log.e(TAG, "Has gravity sensor!");
        }

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyroscope != null) {
            Log.e(TAG, "Has gyroscope sensor!");
        }

        mGyroscope_uncalibrated = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        if (mGyroscope_uncalibrated != null) {
            Log.e(TAG, "Has gyroscope uncalibrate sensor!");
        }

        mLinearAcceration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (mLinearAcceration != null) {
            Log.e(TAG, "Has linear acceleration sensor!");
        }

        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mRotationVector != null) {
            Log.e(TAG, "Has rotation vector sensor!");
        }

        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mStepCounter != null) {
            Log.e(TAG, "Has step counter sensor!");
        }



        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (myList.size() >= 200) {
                    myList.remove(0);
                }

                current_time = System.currentTimeMillis();
                current_accel = sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]);
                //Log.d(TAG, "accel = "+accel+" sec = "+(current_time-previous_time));
                //Log.d(TAG, "System.currentTimeMillis() = "+System.currentTimeMillis());
                //Log.d(TAG, "X: " + String.valueOf(event.values[0]));
                //Log.d(TAG, "Y: " + String.valueOf(event.values[1]));
                //Log.d(TAG, "Z: " + String.valueOf(event.values[2]));
                double time = (((double)(current_time-previous_time))/1000);

                //x_coordinate = x_coordinate + (event.values[0] * time * time)*100;
                //y_coordinate = x_coordinate + (event.values[1] * time * time)*100;
                //z_coordinate = z_coordinate + (event.values[2] * time * time)*100;



                //double velocity = Accel2mms(current_accel, time);
                //double distance = velocity * time / 1000.0;
                x_coordinate = event.values[0] * time * time * 100;
                y_coordinate = event.values[1] * time * time * 100;

                double aX = event.values[0];

                double velocity = current_accel * time;
                double distance = velocity * time ;



                double accel_diff;
                if (current_accel > previous_accel)
                    accel_diff = current_accel - previous_accel;
                else
                    accel_diff = previous_accel - current_accel;

                //if (accel_diff > 0.2)
                    Log.d(TAG, "(x, y) = ("+(int)x_coordinate+", "+(int)y_coordinate+") aX="+String.format("%.4f", event.values[0]*100)+" aY="+String.format("%.4f", event.values[1]*100)+
                            " v="+String.format("%.6f", velocity)+" sec = "+time+" d = "+String.format("%.6f", distance *100));

                if (!is_stop) {

                    Map<String, String> item = new HashMap<String, String>();

                    logMsg = "(x, y) = ("+(int)x_coordinate+", "+(int)y_coordinate+") aX="+String.format("%.4f", event.values[0]*100)+" aY="+String.format("%.4f", event.values[1]*100)+
                            " v="+String.format("%.6f", velocity)+" sec = "+time+" d = "+String.format("%.6f", distance *100);

                    item.put("idx", String.valueOf(myList.size() + 1));
                    item.put("log", logMsg);

                    myList.add(item);


                }

                simpleAdapter.notifyDataSetChanged();

                previous_time = current_time;
                previous_accel = current_accel;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {


            }
        };

        mSensorManager.registerListener(accelerometerListener, mLinearAcceration, SensorManager.SENSOR_DELAY_NORMAL);

        simpleAdapter = new SimpleAdapter(this, myList, R.layout.log_item, new String[] {"idx", "log"}, new int[]{R.id.idx, R.id.log});
        listView.setAdapter(simpleAdapter);

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_stop)
                    is_stop = false;
                else
                    is_stop = true;
            }
        });
    }

    double Accel2mms(double accel, double freq){
        double result = 0;
        result = (gravity*accel)/(2*PI*freq);
        return result;
    }
}
