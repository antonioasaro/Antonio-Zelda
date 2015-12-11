package com.antonio_asaro.zelda;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "zelda";

    private static final String DEVICE_NAME = "ZELDA";
    private static final UUID ZELDA_SERVICE = UUID.fromString("0000ec00-0000-1000-8000-00805f9b34fb");
    private static final UUID ZELDA_CHARACTERISTIC = UUID.fromString("0000ec0e-0000-1000-8000-00805f9b34fb");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final int MAXDEPTH = 16;
    private static final int MINUTES = 5;
    private static final int INTERVALS = 24 * 60 / MINUTES;
    private static final int VIEWSCALE = 32;

    private Date mNow = new Date();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mConnectedGatt;
    private Button mScan;
    private Button mConnect;
    private TextView mScanStatus, mConnectStatus;
    private ProgressDialog mProgress;
    private GraphView mGraphView;
    private ListView mListView;
    private ArrayList<String> mPirValues = new ArrayList();
    private ArrayList<String> mPirList = new ArrayList<>();
    private SharedPreferences mPreferences;
    private boolean mViewType;
    private ArrayAdapter mAdapter;
    private FloatingActionButton mFab;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Alerting the Mrs.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                String addresses[] = new String[1];
                addresses[0] = mPreferences.getString("emailAddress", "Default email address");
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Project Zelda Poops");

                String body = "Last deposits ...\n";
                Date date;
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
                for (int i = 0; i < MAXDEPTH; i++) {
                    String pirDuration = mPirValues.get(i).substring(14, 18);
                    if (!pirDuration.equals("0000")) {
                        try {
                            date = DATE_FORMAT.parse(mPirValues.get(i).substring(0, 14));
                        } catch (Exception e) {
                            Log.d(TAG, "Date conversion failed");
                            return;
                        }
                        body = body + "\n" + sdf.format(date) + " - " + Integer.parseInt(pirDuration) + " secs";
                    }
                }
                intent.putExtra(Intent.EXTRA_TEXT, body + "\n");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
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
//                startScan();
//                ContentValues values = new ContentValues();
//                values.put(PirDataContract.DepositEntry.DAY_OF, mNow.getTime());
//                values.put(PirDataContract.DepositEntry.TIME_OF, "09:23");
//                values.put(PirDataContract.DepositEntry.DURATION_OF, "34");
//                Uri uri = getContentResolver().insert(PirDataContract.CONTENT_URI, values);
            }
        });
        mConnect = (Button) findViewById(R.id.button2);
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectStatus.setText("Connecting ...");
                mPirValues.clear();
//                connectDevice();
//                Cursor cursor = getContentResolver().query(PirDataContract.CONTENT_URI, null, null, null, null);
//                if (cursor.moveToFirst()) {
//                    Log.d(TAG, "Processing cursor from provider: " + cursor.getString(1));
//                    Toast.makeText(getApplicationContext(), "Processing cursor from provider: " + cursor.getString(cursor.getColumnIndex(PirDataContract.DepositEntry.DAY_OF)), Toast.LENGTH_LONG).show();
//                }
            }
        });

        setProgressBarIndeterminate(true);
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        mPirList.add("abc "+ mNow.getTime()); mPirList.add("def " + mNow.getTime());
        mPirList.add("ijk " + mNow.getTime()); mPirList.add("nop "+ mNow.getTime());
        mPirList.add("qrs " + mNow.getTime()); mPirList.add("vce " + mNow.getTime());
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new PirListAdapter(this, R.layout.listview_item, R.id.listText, mPirList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState != SCROLL_STATE_IDLE) mFab.hide(); else mFab.show();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mGraphView = (GraphView) findViewById(R.id.graph);
        mGraphView.setTitle("    Last day's deposits -->");
        mGraphView.getViewport().setScrollable(true);
        mGraphView.getViewport().setScalable(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(INTERVALS / VIEWSCALE);
        GridLabelRenderer gridLabel = mGraphView.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("    Time (24hr)");
        gridLabel.setVerticalAxisTitle("Duration (sec)");
        gridLabel.setLabelsSpace(0);
        gridLabel.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                long newTime = mNow.getTime() - (long) ((value * MINUTES * 60) * 1000);
                Date adjDate = new Date(newTime);
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(adjDate);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                String adjMin = ":" + ((minute < 10) ? ("0" + Integer.toString(minute)) : Integer.toString(minute));
                if (isValueX) {
                    return super.formatLabel(hour, true) + adjMin;
                } else {
                    return super.formatLabel(value, false);
                }
            }
        });

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    class PirListAdapter extends ArrayAdapter<String> {
        PirListAdapter(Context c, int i, int j, ArrayList<String> s) {
            super(c, i, j, s);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ImageView iv = (ImageView) row.findViewById(R.id.listImage);
            if (position > 1) {
                iv.setImageResource(R.mipmap.ic_pee);
            } else {
                iv.setImageResource(R.mipmap.ic_poop);
            }
            return row;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mViewType = mPreferences.getBoolean("viewType", true);
        if (mViewType) {
            mGraphView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mGraphView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.antonio_asaro.zelda/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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

    private void createList() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
        for (int i = 0; i < MAXDEPTH; i++) {
            String pirDuration = mPirValues.get(i).substring(14, 18);
            if (!pirDuration.equals("0000")) {
                try {
                    Date date = DATE_FORMAT.parse(mPirValues.get(i).substring(0, 14));
                    mPirList.add(sdf.format(date) + " - " + Integer.parseInt(pirDuration) + " secs");
                } catch (Exception e) {
                    Log.d(TAG, "Date conversion failed");
                    return;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void drawGraph() {
        int i, delta, duration;
        Date pirDate;
        DataPoint[] dataPoints = new DataPoint[INTERVALS];

        for (i = 0; i < INTERVALS; i++) {
            dataPoints[i] = new DataPoint(i, 0);
        }

        mNow = new Date();
        for (i = 0; i < MAXDEPTH; i++) {
            try {
                pirDate = DATE_FORMAT.parse(mPirValues.get(i).substring(0, 14));
            } catch (Exception e) {
                Log.d(TAG, "Date conversion failed");
                return;
            }
            delta = (int) Math.abs((mNow.getTime() - pirDate.getTime()) / 1000 / (60 * MINUTES));
            if (delta < INTERVALS) {
                duration = Integer.parseInt(mPirValues.get(i).substring(14, 18));
                dataPoints[delta] = new DataPoint(delta, duration + dataPoints[delta].getY());
                Log.d(TAG, "Data point is: " + "(" + delta + " " + ", " + duration + ")");
            }
        }

        BarGraphSeries<DataPoint> dataSeries = new BarGraphSeries<>(dataPoints);
        dataSeries.setColor(Color.rgb(0, 128, 0));
        mGraphView.removeAllSeries();
        mGraphView.addSeries(dataSeries);
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
        public void handleMessage(Message msg) {
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
                    String pirValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString();
                    for (int i = 1; i < 18; i++) {
                        pirValue = pirValue + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i).toString();
                    }
                    mPirValues.add(pirValue);
                    if (msg.what == MSG_LAST) {
                        mProgress.hide();
                        Collections.sort(mPirValues, Collections.reverseOrder());
                        drawGraph();
                        createList();
                        mConnectStatus.setText("Successful xfer of stats");
                    }
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.antonio_asaro.zelda/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }
}
