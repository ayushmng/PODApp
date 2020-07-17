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
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.ContentPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.AwsConfigListAdapter;
import np.com.bottle.podapp.adapter.WifiListAdapter;
import np.com.bottle.podapp.fragment.WifiConfigFragment;
import np.com.bottle.podapp.interfaces.OnItemClickListener;
import np.com.bottle.podapp.models.DataList;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class SettingsActivity extends AppCompatActivity implements OnItemClickListener {

    private static String TAG = SettingsActivity.class.getSimpleName();

    private RecyclerView rvWifi;
    private RecyclerView rvDeviceDetails;
    private Switch swKiosk;
    private Button btnClearContent;

    private AppPreferences appPref;
    private ContentPreferences contentPref;
    private List<DataList> deviceDetailList;

    private WifiManager mWifiManager;
    private List<ScanResult> wifiList;
    private WifiListAdapter adapter;
    private ScanResult selectedScanResult;

    private AwsConfigListAdapter deviceDetailsAdapter;
    private PODApp appClass;

    // AWS
    private AWSIotMqttManager mqttManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rvWifi = findViewById(R.id.rvWifi);
        rvDeviceDetails = findViewById(R.id.rvDeviceDetails);
        swKiosk = findViewById(R.id.swKiosk);
        btnClearContent = findViewById(R.id.btnClearContent);

        appClass = (PODApp) getApplication();
        mqttManager = appClass.getMqttManager();
        appPref = new AppPreferences(getApplicationContext());
        deviceDetailList = new ArrayList<>();
        wifiList = new ArrayList<>();
        contentPref = new ContentPreferences(getApplicationContext());

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

        if (appClass.isAWSConnected) {
            subscribeToAndroidCommand();
        }

        swKiosk.setChecked(appPref.getBoolean(AppPreferences.IS_KIOSK_MODE));

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                assert mWifiManager != null;
                mWifiManager.startScan();
            }
        };
        timer.schedule(timerTask, 1000, 10000);

        swKiosk.setOnCheckedChangeListener(swKioskClickListener);
        btnClearContent.setOnClickListener(btnClearContentListener);
    }

    private View.OnClickListener btnClearContentListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            contentPref.clear();
        }
    };

    private CompoundButton.OnCheckedChangeListener swKioskClickListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            String commandPayload = "";
            if(b) {
                commandPayload = Helper.generateMqttCommandPayload(
                        appPref.getString(AppPreferences.DEVICE_ID),
                        Constants.PAYLOAD_TYPE_COMMAND,
                        "enable",
                        "kiosk",
                        "np.com.bottle.podapp");
            } else {
                commandPayload = Helper.generateMqttCommandPayload(
                        appPref.getString(AppPreferences.DEVICE_ID),
                        Constants.PAYLOAD_TYPE_COMMAND,
                        "disable",
                        "kiosk",
                        "np.com.bottle.podapp");
            }

            Log.d(TAG, "command payload: " + commandPayload);
            publishMsg(Constants.TOPIC_ANDROID_PUB, commandPayload);
        }
    };

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
        deviceDetailList.add(new DataList(AppPreferences.ACTIVATION_CODE, appPref.getString(AppPreferences.ACTIVATION_CODE)));
//        deviceDetailList.add(new DataList(AppPreferences.DEVICE_URI, appPref.getString(AppPreferences.DEVICE_URI)));
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

    // Publish message to the device_uri topic.
    // QOS 1 is being used for mqtt.
    private void publishMsg(String topic, String payload) {
        mqttManager.publishString(payload, topic, AWSIotMqttQos.QOS1);
        Log.d(TAG, topic + ": Message Sent");
    }

    private void subscribeToAndroidCommand() {
        try {
            mqttManager.subscribeToTopic(Constants.TOPIC_ANDROID_SUB, AWSIotMqttQos.QOS1,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(String topic, byte[] data) {
                            String strPayload = new String(data, StandardCharsets.UTF_8);

                            Log.d(TAG, "topic: " + topic);
                            Log.d(TAG, "Message: " + strPayload);
                            try {
                                JSONObject parentPayload = new JSONObject(strPayload);
                                JSONObject essentialPayload = parentPayload.getJSONObject("essential");
                                JSONObject payload = essentialPayload.getJSONObject("payload");

                                if (essentialPayload.getString("payloadType").equals(Constants.PAYLOAD_TYPE_COMMAND)
                                        && payload.getString("module").equals("kiosk")) {
                                    if(payload.getString("action").equals("enable")) {
                                        appPref.putBoolean(AppPreferences.IS_KIOSK_MODE, true);
                                    } else {
                                        appPref.putBoolean(AppPreferences.IS_KIOSK_MODE, false);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "JSONException: " + e.getMessage());
                            }

                        }
                    });
            Log.d(TAG, "Subscribed to topic: " + Constants.TOPIC_ANDROID_SUB);
        } catch (Exception e) {
            Log.e(TAG, "Error in Subscribing to Topic.");
        }
    }
}