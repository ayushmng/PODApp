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
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.AwsConfigListAdapter;
import np.com.bottle.podapp.aws.AwsIotHelper;
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
    private Button btnProvision;

    private AWSIotMqttManager mqttManager;
    private KeyStore clientKeyStore;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning);

        // region Initialization
        tvIpAddress = findViewById(R.id.tvIpAddress);
        tvPort = findViewById(R.id.tvPort);
        rvAwsConfig = findViewById(R.id.rvAwsConfig);
        btnProvision = findViewById(R.id.btnProvision);

        appPref = new AppPreferences(getApplicationContext());
        rvDataList = new ArrayList<DataList>();
        // endregion

        checkPermission();

        ipAddress = Helper.getIpAddress(getApplicationContext());

        updateUI();
        startSocketServer();
        populateData();

        rvAdapter = new AwsConfigListAdapter(rvDataList);
        rvAwsConfig.setAdapter(rvAdapter);
        rvAwsConfig.setLayoutManager(new LinearLayoutManager(this));

//        connectToAws();

        btnProvision.setOnClickListener(btnProvisionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            wSocket.stop();
        } catch (Exception e) {
            Log.e(TAG, "Error closing web socket");
        }
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
            String strCert = downloadFile(data.getString("certificate_url"));
            String strPrivateKey = downloadFile(data.getString("privateKey_url"));
            String strPublicKey = downloadFile(data.getString("publicKey_url"));
            String keystorePath = getFilesDir().getPath();
            String endpoint = appPref.getString(AppPreferences.MQTT_HOST);
            AwsIotHelper.saveConfiguration(endpoint, Constants.CERTIFICATE_ID, strCert, strPrivateKey, strPublicKey, keystorePath);
            Log.d(TAG, "aws key saved.");

            connectToAws();
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
        }
    }

    public String downloadFile(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            String str = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "file content: " + str);
            return str;
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves the AWS config data into shared preferences.
     */
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

    private void populateData() {
        rvDataList.clear();
        rvDataList.add(new DataList(AppPreferences.DEVICE_NAME, appPref.getString(AppPreferences.DEVICE_NAME)));
        rvDataList.add(new DataList(AppPreferences.DEVICE_ID, appPref.getString(AppPreferences.DEVICE_ID)));
        rvDataList.add(new DataList(AppPreferences.ACTIVATION_TOKEN, appPref.getString(AppPreferences.ACTIVATION_TOKEN)));
        rvDataList.add(new DataList(AppPreferences.DEVICE_URI, appPref.getString(AppPreferences.DEVICE_URI)));
        rvDataList.add(new DataList(AppPreferences.FLEET_ID, appPref.getString(AppPreferences.FLEET_ID)));
        rvDataList.add(new DataList(AppPreferences.MQTT_HOST, appPref.getString(AppPreferences.MQTT_HOST)));
    }

    private void updateUI() {
        if (ipAddress != null)
            tvIpAddress.setText(ipAddress);
        tvPort.setText(String.format("%s", Constants.PORT));
    }

    /**
     * Starts the web socket server on the port as specified in the Constants.PORT
     */
    private void startSocketServer() {
        try {
            wSocket = new WebSocketHelper(this, Constants.PORT);
            wSocket.start();
            Log.d(TAG, "WebSocket started on port: " + wSocket.getPort());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void connectToAws() {
        try {
            String keystorePath = getFilesDir().getPath();
            clientId = appPref.getString(AppPreferences.DEVICE_NAME) + "_" + appPref.getString(AppPreferences.DEVICE_ID);
            Log.d(TAG, "client id: " + clientId);
            Log.d(TAG, "endpoint: " + appPref.getString(AppPreferences.MQTT_HOST));
            Log.d(TAG, "Sug topic = " + Constants.TOPIC_ACTIVATE + "_" + appPref.getString(AppPreferences.DEVICE_ID));

            mqttManager = new AWSIotMqttManager(clientId, appPref.getString(AppPreferences.MQTT_HOST));
            mqttManager.setKeepAlive(10);
            clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(Constants.CERTIFICATE_ID,
                    keystorePath, Constants.KEYSTORE_NAME, Constants.KEYSTORE_PASSWORD);

            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    Log.d(TAG, "Status = " + String.valueOf(status));
                    if(status == AWSIotMqttClientStatus.Connected)
                        activateDevice();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error AWS Connection: " + e.getMessage());
        }
    }

    // Creation of activation payload.
    // Calls publishMsg() method.
    private void activateDevice() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", "activate");
            json.put("activationCode", appPref.getString(AppPreferences.ACTIVATION_TOKEN));
        } catch (Exception e) {
            Log.e(TAG, "Error in creating json object.");
        }

        publishMsg(json.toString());
    }

    // Publish activation message to the activation topic.
    // QOS 1 is being used for mqtt.
    private void publishMsg(String payload) {
        subscribe(Constants.TOPIC_ACTIVATE + "_" + appPref.getString(AppPreferences.DEVICE_ID));

        String topic = Constants.TOPIC_ACTIVATE;
        mqttManager.publishString(payload, topic, AWSIotMqttQos.QOS1);
        Log.d(TAG, "Activation Message Sent");
    }

    private void subscribe(String topic) {
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(String topic, byte[] data) {
                            Log.d(TAG, "here sub");
                            String strData = new String(data, StandardCharsets.UTF_8);
                            JSONObject json = null;
                            try {
                                json = new JSONObject(strData);
                                if(json.getString("deviceId").equals(appPref.getString(AppPreferences.DEVICE_ID))) {
                                    appPref.putBoolean(AppPreferences.IS_PROVISIONED, true);

                                }

                            } catch (JSONException e) {
                                Log.e(TAG, "Error in parsing json string.");
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in Subscribing to Topic.");
        }
    }

    private View.OnClickListener btnProvisionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            connectToAws();
        }
    };

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
