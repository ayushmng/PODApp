package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.AwsConfigListAdapter;
import np.com.bottle.podapp.interfaces.WebsocketListener;
import np.com.bottle.podapp.models.DataList;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;
import np.com.bottle.podapp.util.WebSocketHelper;

public class ProvisioningActivity extends AppCompatActivity implements WebsocketListener {

    private static String TAG = ProvisioningActivity.class.getSimpleName();
    private final int REQUEST_CODE_ACCESS_WIFI_STATE = 1;

    private AppPreferences appPref;
    private WebSocketHelper wSocket;
    private String ipAddress;
    private List<DataList> rvDataList;
    private AwsConfigListAdapter rvAdapter;

    private TextView tvIpAddress;
    private TextView tvPort;
    private RecyclerView rvAwsConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning);

        // region Initialization
        tvIpAddress = findViewById(R.id.tvIpAddress);
        tvPort = findViewById(R.id.tvPort);
        rvAwsConfig = findViewById(R.id.rvAwsConfig);

        appPref = new AppPreferences(getApplicationContext());
        rvDataList = new ArrayList<DataList>();
        // endregion

        checkPermission();

        ipAddress = Helper.getIpAddress(getApplicationContext());

        updateUI();
        startSocketServer();

        populateData();
        Log.d(TAG, "count: " + rvDataList.size());
        rvAdapter = new AwsConfigListAdapter(rvDataList);
        rvAwsConfig.setAdapter(rvAdapter);
        rvAwsConfig.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateUI() {
        if (ipAddress != null)
            tvIpAddress.setText(ipAddress);
        tvPort.setText(String.format("%s", Constants.PORT));
    }

    private void populateData() {
        rvDataList.clear();
        rvDataList.add(new DataList(AppPreferences.DEVICE_NAME, appPref.getString(AppPreferences.DEVICE_NAME)));
        rvDataList.add(new DataList(AppPreferences.DEVICE_ID, appPref.getString(AppPreferences.DEVICE_ID)));
        rvDataList.add(new DataList(AppPreferences.ACTIVATION_TOKEN, appPref.getString(AppPreferences.ACTIVATION_TOKEN)));
        rvDataList.add(new DataList(AppPreferences.DEVICE_URI, appPref.getString(AppPreferences.DEVICE_URI)));
        rvDataList.add(new DataList(AppPreferences.FLEET_ID, appPref.getString(AppPreferences.FLEET_ID)));
        rvDataList.add(new DataList(AppPreferences.MQTT_HOST, appPref.getString(AppPreferences.MQTT_HOST)));
    }

    /**
     * Used for listening websocket messages.
     * @param configData -> payload
     */
    @Override
    public void onAwsConfigReceived(String configData) {
        Log.d(TAG, "onAwsConfigReceived: " + configData);

        try {
            JSONObject data = new JSONObject(configData);
            saveAwsConfigData(data);
            Log.d(TAG, "data: " + data.getString("type"));
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
        }
    }

    public void saveAwsConfigData(JSONObject data) throws JSONException {
        appPref.putString(AppPreferences.DEVICE_NAME, data.getString("name"));
        appPref.putString(AppPreferences.DEVICE_ID, data.getString("device_id"));
        appPref.putString(AppPreferences.ACTIVATION_TOKEN, data.getString("activation"));
        appPref.putString(AppPreferences.DEVICE_URI, data.getString("device_uri"));
        appPref.putString(AppPreferences.FLEET_ID, data.getString("fleet_id"));
        appPref.putString(AppPreferences.MQTT_HOST, data.getString("mqtt_host"));

        populateData();
        rvAdapter.notifyDataSetChanged();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
    }

    private void startSocketServer() {
        try {
            wSocket = new WebSocketHelper(this, Constants.PORT);
            wSocket.start();
            Log.d(TAG, "ChatServer started on port: " + wSocket.getPort());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    // region Permission Section
    /**
     * This method is used for checking the permission for ACCESS_WIFI_STATE
     */
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_CODE_ACCESS_WIFI_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_WIFI_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "WiFi Access State Permission Granted.");
                } else {
                    Log.e(TAG, "WiFi Access State Permission Not Granted");
                }
        }
    }

    // endregion
}
