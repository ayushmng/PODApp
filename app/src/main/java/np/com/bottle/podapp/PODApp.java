package np.com.bottle.podapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;

import java.security.KeyStore;

import np.com.bottle.podapp.util.Constants;

public class PODApp extends Application {

    private static String TAG = PODApp.class.getSimpleName();
    private AppPreferences appPref;
    private AWSIotMqttManager mqttManager;
    public boolean isAWSConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        appPref = new AppPreferences(getApplicationContext());

        if(appPref.getBoolean(AppPreferences.IS_PROVISIONED)) {
            connectToAws();
        }
    }

    private void connectToAws() {
        try {
            String keystorePath = getFilesDir().getPath();
            String clientId = appPref.getString(AppPreferences.DEVICE_NAME) + "_" + appPref.getString(AppPreferences.DEVICE_ID);
            Log.d(TAG, "client id: " + clientId);
            Log.d(TAG, "endpoint: " + appPref.getString(AppPreferences.MQTT_HOST));
            Log.d(TAG, "Sug topic = " + Constants.TOPIC_ACTIVATE + "_" + appPref.getString(AppPreferences.DEVICE_ID));

            mqttManager = new AWSIotMqttManager(clientId, appPref.getString(AppPreferences.MQTT_HOST));
            mqttManager.setKeepAlive(10);
            KeyStore clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(Constants.CERTIFICATE_ID,
                    keystorePath, Constants.KEYSTORE_NAME, Constants.KEYSTORE_PASSWORD);

            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    Log.d(TAG, "Status = " + String.valueOf(status));
                    if(status == AWSIotMqttClientStatus.Connected) {
                        isAWSConnected = true;
                    } else {
                        isAWSConnected = false;
                    }

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error AWS Connection: " + e.getMessage());
        }
    }

    public AWSIotMqttManager getMqttManager() {
        return mqttManager;
    }

    public boolean getAWSStatus() {
        return isAWSConnected;
    }

}
