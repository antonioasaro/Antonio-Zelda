package com.antonio_asaro.www.zelda;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "zelda";
    private static final String DEVICE_NAME = "ZELDA";
    private static final UUID ZELDA_SERVICE         = UUID.fromString("0000ec00-0000-1000-8000-00805f9b34fb");
    private static final UUID ZELDA_CHARACTERISTIC  = UUID.fromString("0000ec0e-0000-1000-8000-00805f9b34fb");
    private static final int MAXDEPTH = 8;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mConnectedGatt;
    private Button mScan, mConnect;
    private TextView mScanStatus, mConnectStatus;
    private ProgressDialog mProgress;
    private GraphView mGraphView;
    private ArrayList<Long> mPirDates = new ArrayList();
    private ArrayList<Integer> mPirDuration = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        mScanStatus = (TextView) findViewById(R.id.textView3);
        mConnectStatus = (TextView) findViewById(R.id.textView2);
        mScan = (Button) findViewById(R.id.button);
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanStatus.setText("Scanning ...");
                mDevice = null;
                startScan();
            }
        });
        mConnect = (Button) findViewById(R.id.button2);
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectStatus.setText("Connecting ...");
                mPirDates.clear();
                mPirDuration.clear();
                connectDevice();
            }
        });

        setProgressBarIndeterminate(true);
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        mGraphView = (GraphView) findViewById(R.id.graph);
        mGraphView.setTitle("Last 24hrs");
        GridLabelRenderer gridLabel = mGraphView.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Date");
        gridLabel.setVerticalAxisTitle("Duration");


        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgress.dismiss();
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);
        mHandler.postDelayed(mStopRunnable, 2000);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
        if (mDevice == null) mScanStatus.setText("No devices found!!");
    }

    private void connectDevice() {
        mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting ..."));
        BluetoothDevice device = mDevice;
        mConnectedGatt = device.connectGatt(getApplicationContext(), true, mGattCallback);
    }

    private void drawGraph() {
        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, mPirDuration.get(0)), new DataPoint(1, mPirDuration.get(1)),
                new DataPoint(2, mPirDuration.get(2)), new DataPoint(3, mPirDuration.get(3)),
                new DataPoint(4, mPirDuration.get(4)), new DataPoint(5, mPirDuration.get(5)),
                new DataPoint(6, mPirDuration.get(6)), new DataPoint(7, mPirDuration.get(7))
        });
        series.setColor(Color.rgb(0, 128, 0));
        mGraphView.removeAllSeries();
        mGraphView.addSeries(series);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        if (DEVICE_NAME.equals(device.getName())) {
            mScanStatus.setText("Found: " + device.getName());
            mDevice = device;
            stopScan();
            mHandler.removeCallbacks(mStopRunnable);
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        private int loopCount;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                mHandler.sendEmptyMessage(MSG_CONNECT);
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services ..."));
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                mHandler.sendEmptyMessage(MSG_DISCONNECT);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattCharacteristic characteristic;
            Log.d(TAG, "Services Discovered: " + status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Reading Characteristics ..."));
            characteristic = gatt.getService(ZELDA_SERVICE).getCharacteristic(ZELDA_CHARACTERISTIC);
            loopCount = 0;
            gatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "OnCharacteristicRead: " + status + " " + loopCount);
            if (loopCount == (MAXDEPTH - 1)) {
                mHandler.sendMessage(Message.obtain(null, MSG_LAST, characteristic));
            } else {
                mHandler.sendMessage(Message.obtain(null, MSG_CHARACTERISTIC, characteristic));
            }
            if (loopCount < MAXDEPTH - 1) {
                loopCount = loopCount + 1;
                gatt.readCharacteristic(characteristic);
            }
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    private static final int MSG_CONNECT = 100;
    private static final int MSG_DISCONNECT = 101;
    private static final int MSG_PROGRESS = 102;
    private static final int MSG_CHARACTERISTIC = 103;
    private static final int MSG_LAST = 104;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            BluetoothGattCharacteristic characteristic;

            Log.d(TAG, "Calling handler with " + msg.toString());
            switch (msg.what) {
                case MSG_CONNECT:
                    mConnectStatus.setText("Connected");
                    break;
                case MSG_DISCONNECT:
                    mConnectStatus.setText("Disconnected");
                    break;
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_CHARACTERISTIC:
                case MSG_LAST:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining characteristic value");
                        return;
                    }
                    int i; int val; Long pirDate; int pirDuration;
                    pirDate = Long.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                    for (i=1; i<18-4; i++) {
                        val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i);
                        pirDate = 10 * pirDate + val;
                    }
                    pirDuration = Integer.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 18-4));
                    for (i=18-4+1; i<18; i++) {
                        val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i);
                        pirDuration = 10 * pirDuration + val;
                    }
                    mPirDates.add(pirDate);
                    mPirDuration.add(pirDuration);
                    if (msg.what == MSG_LAST) {
                        mProgress.hide();
                        Collections.sort(mPirDates, Collections.reverseOrder());
                        drawGraph();
                        mConnectStatus.setText("Stats: " + mPirDates.get(0).toString() + " " + mPirDuration.get(0).toString());
                    }
                    break;
            }
        }
    };

};
