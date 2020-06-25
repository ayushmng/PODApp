package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.AwsConfigListAdapter;
import np.com.bottle.podapp.adapter.WifiListAdapter;
import np.com.bottle.podapp.fragment.WifiConfigFragment;
import np.com.bottle.podapp.interfaces.OnItemClickListener;
import np.com.bottle.podapp.models.DataList;

public class SettingsActivity extends AppCompatActivity implements OnItemClickListener {

    private static String TAG = SettingsActivity.class.getSimpleName();

    private RecyclerView rvWifi;
    private RecyclerView rvDeviceDetails;

    private AppPreferences appPref;
    private List<DataList> deviceDetailList;

    private WifiManager mWifiManager;
    private List<ScanResult> wifiList;
    private WifiListAdapter adapter;
    private ScanResult selectedScanResult;

    private AwsConfigListAdapter deviceDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rvWifi = findViewById(R.id.rvWifi);
        rvDeviceDetails = findViewById(R.id.rvDeviceDetails);

        appPref = new AppPreferences(getApplicationContext());
        deviceDetailList = new ArrayList<>();
        wifiList = new ArrayList<>();

        // Wifi RecyclerView Section
        populateWifiList(wifiList);
        adapter = new WifiListAdapter(this, wifiList);
        rvWifi.setAdapter(adapter);
        rvWifi.setLayoutManager((new LinearLayoutManager(this)));
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        populateDeviceDetails();
        deviceDetailsAdapter = new AwsConfigListAdapter(deviceDetailList);
        rvDeviceDetails.setAdapter(deviceDetailsAdapter);
        rvDeviceDetails.setLayoutManager(new LinearLayoutManager(this));


        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                assert mWifiManager != null;
                mWifiManager.startScan();
            }
        };
        timer.schedule(timerTask, 1000, 10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                // add your logic here

                populateWifiList(mScanResults);
                adapter.notifyDataSetChanged();

            }
        }
    };

    private void populateWifiList(List<ScanResult> scanResults) {
        wifiList.clear();

        for (ScanResult result : scanResults) {
            if(!result.SSID.equals("")) {
                wifiList.add(result);
            }
        }
    }

    private void populateDeviceDetails() {
        deviceDetailList.clear();
        deviceDetailList.add(new DataList(AppPreferences.DEVICE_NAME, appPref.getString(AppPreferences.DEVICE_NAME)));
        deviceDetailList.add(new DataList(AppPreferences.DEVICE_ID, appPref.getString(AppPreferences.DEVICE_ID)));
        deviceDetailList.add(new DataList(AppPreferences.ACTIVATION_TOKEN, appPref.getString(AppPreferences.ACTIVATION_TOKEN)));
        deviceDetailList.add(new DataList(AppPreferences.DEVICE_URI, appPref.getString(AppPreferences.DEVICE_URI)));
        deviceDetailList.add(new DataList(AppPreferences.FLEET_ID, appPref.getString(AppPreferences.FLEET_ID)));
        deviceDetailList.add(new DataList(AppPreferences.MQTT_HOST, appPref.getString(AppPreferences.MQTT_HOST)));
    }

    @Override
    public void onItemClicked(ScanResult scanResult) {
        selectedScanResult = scanResult;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putParcelable(WifiConfigFragment.ARG_PARAM1, scanResult);

        DialogFragment dialogFragment = new WifiConfigFragment(this);
        dialogFragment.setArguments(args);
        dialogFragment.show(ft, "dialog");
    }
}