package com.seventhmoon.sensortest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.seventhmoon.sensortest.Bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.DEVICE_NAME;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.MESSAGE_DEVICE_NAME;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.MESSAGE_READ;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.MESSAGE_STATE_CHANGE;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.MESSAGE_TOAST;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.MESSAGE_WRITE;
import static com.seventhmoon.sensortest.Bluetooth.Data.Constants.TOAST;
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
    private SensorEventListener rotationVectorListener;

    private static double PI = 3.1415926535897932384626433832795;
    private static double gravity = 9806.65;

    private static long previous_time = 0;
    private static long current_time = 0;
    private static double x_previous_velocity = 0.0;
    private static double y_previous_velocity = 0.0;
    private static double x_current_velocity = 0.0;
    private static double y_current_velocity = 0.0;
    private static double previous_accel = 0.0;
    private static double current_accel = 0.0;

    private static long x_coordinate_current = 0;
    private static long y_coordinate_current = 0;
    private static long x_coordinate_previous = 0;
    private static long y_coordinate_previous = 0;
    private static double distance = 0;

    private ListView listView;
    private SimpleAdapter simpleAdapter;

    private static List<Map<String, String>> myList = new ArrayList<>();
    private static String logMsg;
    private static boolean is_stop = false;

    //bluetooth
    /**
     * Name of the connected device
     */
    public static String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    //private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    public static StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    public static BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    public static BluetoothService mChatService = null;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private MenuItem item_bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "This device is not support bluetooth");
        } else {
            if (mBluetoothAdapter.isEnabled()) {

                Log.d(TAG, "Bluetooth is enabled");
            } else {

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }


        listView = (ListView) findViewById(R.id.listView);
        Button btnStop = (Button) findViewById(R.id.btnStopOrPlay);
        Button btnReset = (Button) findViewById(R.id.btnReset);

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



        previous_time = 0;
        current_time = 0;
        x_previous_velocity = 0.0;
        y_previous_velocity = 0.0;
        x_current_velocity = 0.0;
        y_current_velocity = 0.0;
        previous_accel = 0.0;
        current_accel = 0.0;

        x_coordinate_current = 0;
        y_coordinate_current = 0;

        distance = 0.0;

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                double time;
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
                if (previous_time != 0) {
                    time = (((double) (current_time - previous_time)) / 1000);

                    //x_coordinate = x_coordinate + (event.values[0] * time * time)*100;
                    //y_coordinate = x_coordinate + (event.values[1] * time * time)*100;
                    //z_coordinate = z_coordinate + (event.values[2] * time * time)*100;


                    //Log.d(TAG, "time = " + time);


                    //double velocity = Accel2mms(current_accel, time);
                    //double distance = velocity * time / 1000.0;
                    //x_current_velocity = x_previous_velocity + event.values[0] * time * 100;
                    //y_current_velocity = y_previous_velocity + event.values[1] * time * 100;
                    //double x_velocity = event.values[0] * time * 100;
                    //double y_velocity = event.values[1] * time * 100;
                    x_coordinate_current = (long)(event.values[0] * time * time * 100);
                    y_coordinate_current = (long)(event.values[1] * time * time * 100);
                    x_current_velocity =  (x_coordinate_current - x_coordinate_previous)/time ;
                    y_current_velocity =  (y_coordinate_current - y_coordinate_previous)/time;



                    double velocity = current_accel * time;
                    if (x_coordinate_current != 0 && y_coordinate_current != 0) {
                        //distance = distance + velocity * time;
                        distance = distance + sqrt(x_current_velocity*x_current_velocity+y_current_velocity*y_current_velocity) * time;
                        //Log.d(TAG, "distance = "+distance);
                    }



                    if (!is_stop) {

                        //if (accel_diff > 0.2)
                        /*Log.d(TAG, "(" + x_coordinate_current + ", " + y_coordinate_current +
                                ")" +
                                " vX=" +
                                String.format("%.3f", x_current_velocity) +
                                " vY=" +
                                String.format("%.3f", y_current_velocity) +
                                " aX=" +
                                String.format("%.3f", event.values[0] * 100) +
                                " aY=" +
                                String.format("%.3f", event.values[1] * 100) +
                                " sec = " +
                                time +
                                " d = " +
                                distance );*/


                        Map<String, String> item = new HashMap<String, String>();

                        logMsg = "(" +  x_coordinate_current + ", " +  y_coordinate_current +
                                ")" +
                                " vX=" +
                                String.format("%.3f", x_current_velocity) +
                                " vY=" +
                                String.format("%.3f", y_current_velocity) +
                                " aX=" +
                                String.format("%.3f", event.values[0] * 100) +
                                " aY=" +
                                String.format("%.3f", event.values[1] * 100) +
                                " sec = " +
                                time +
                                " d = " +
                                distance ;

                        item.put("idx", String.valueOf(myList.size() + 1));
                        item.put("log", logMsg);

                        myList.add(item);
                        //send to blue tooth
                        //String message = "Oppt score";
                        byte[] send = logMsg.getBytes();
                        if (mChatService != null) {
                            mChatService.write(send);

                            // Reset out string buffer to zero and clear the edit text field
                            mOutStringBuffer.setLength(0);
                        }

                    }


                }

                simpleAdapter.notifyDataSetChanged();

                previous_time = current_time;
                previous_accel = current_accel;
                //x_previous_velocity = x_current_velocity;
                //y_previous_velocity = y_current_velocity;
                x_coordinate_previous = x_coordinate_current;
                y_coordinate_previous = y_coordinate_current;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {


            }
        };

        mSensorManager.registerListener(accelerometerListener, mLinearAcceration, SensorManager.SENSOR_DELAY_NORMAL);

        rotationVectorListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                //Log.d(TAG, "x : "+event.values[0]+" y : "+event.values[1]+" z : "+event.values[2]+" scalar : "+event.values[3]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mSensorManager.registerListener(rotationVectorListener, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);

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

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x_current_velocity = 0.0;
                y_current_velocity = 0.0;
                x_previous_velocity = 0.0;
                y_previous_velocity = 0.0;
                distance = 0.0;
            }
        });
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                if (mChatService != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mChatService.getState() == BluetoothService.STATE_NONE) {
                        // Start the Bluetooth chat services
                        mChatService.start();
                    }
                } else {
                    Log.d(TAG, "mChatService = null");
                    setupChat();
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        item_bluetooth = menu.findItem(R.id.action_bluetooth);

        if (mBluetoothAdapter == null)
            item_bluetooth.setVisible(false);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_bluetooth:

                if (!mBluetoothAdapter.isEnabled()) {
                    intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    // Otherwise, setup the chat session
                } else if (mChatService == null) {
                    setupChat();
                } else {
                    intent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
                }


                break;

            default:
                break;
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth was not enabled.",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d(TAG, "STATE_CONNECTED");
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "STATE_CONNECTING");
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.d(TAG, "STATE_LISTEN");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.d(TAG, "STATE_NONE");
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "MESSAGE_READ : "+readMessage);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        // = new ArrayAdapter<>(this, R.layout.message);

        //mConversationView.setAdapter(mConversationArrayAdapter);

        /*
        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });*/

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    double Accel2mms(double accel, double freq){
        double result = 0;
        result = (gravity*accel)/(2*PI*freq);
        return result;
    }
}
