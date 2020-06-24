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
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.WifiListAdapter;
import np.com.bottle.podapp.fragment.NfcDetectFragment;
import np.com.bottle.podapp.fragment.WifiConfigFragment;
import np.com.bottle.podapp.interfaces.OnItemClickListener;

public class SettingsActivity extends AppCompatActivity implements OnItemClickListener {

    private static String TAG = SettingsActivity.class.getSimpleName();

    private RecyclerView rvWifi;

    private WifiManager mWifiManager;
    private List<ScanResult> wifiList;
    private WifiListAdapter adapter;
    private ScanResult selectedScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rvWifi = findViewById(R.id.rvWifi);

        wifiList = new ArrayList<>();
        populateWifiList(wifiList);
        adapter = new WifiListAdapter(this, wifiList);
        rvWifi.setAdapter(adapter);
        rvWifi.setLayoutManager((new LinearLayoutManager(this)));

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


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