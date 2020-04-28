package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.nfclib.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.nfc.KeyInfoProvider;
import np.com.bottle.podapp.nfc.NfcAppKeys;
import np.com.bottle.podapp.nfc.NfcFileType;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class AdDisplayActivity extends AppCompatActivity {

    private static String TAG = AdDisplayActivity.class.getSimpleName();
    private AppPreferences appPref;
    PODApp appClass;

    private NfcAdapter nfcAdapter;
    private NxpNfcLib libInstance;
    private IKeyData objKEY_AES128;

    private AWSIotMqttManager mqttManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_display);

        appPref = new AppPreferences(getApplicationContext());
        appClass = (PODApp) getApplication();

        checkPermission();
        initializedLibrary();
        initializeKeys();

        mqttManager = appClass.getMqttManager();

        if(appClass.getAWSStatus()) {
            publishMsg("Hello from android!!!!!!");
            subscribe(appPref.getString(AppPreferences.DEVICE_URI));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    // Publish message to the device_uri topic.
    // QOS 1 is being used for mqtt.
    private void publishMsg(String payload) {
        String topic = appPref.getString(AppPreferences.DEVICE_URI);
        mqttManager.publishString(payload, topic, AWSIotMqttQos.QOS1);
        Log.d(TAG, topic + ": Message Sent");
    }

    private void subscribe(String topic) {
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(String topic, byte[] data) {
                            Log.d(TAG, "here sub");
                            String strData = new String(data, StandardCharsets.UTF_8);
                            Log.d(TAG, "Message: " + strData);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in Subscribing to Topic.");
        }
    }


    // region NFC Initialization
    private void initializedLibrary() {
        Log.d(TAG, "initializedLibrary");
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, NfcAppKeys.LICENSE_KEY);
        } catch (NxpNfcLibException ex) {
            Log.e(TAG, "NxpNfcLibException: " + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void initializeKeys() {
        KeyInfoProvider infoProvider = KeyInfoProvider.getInstance(getApplicationContext());
        infoProvider.setKey(Helper.ALIAS_KEY_AES128, NfcAppKeys.EnumKeyType.EnumAESKey, NfcAppKeys.KEY_AES128_AllAccess);

        KeyData keyDataObj = new KeyData();
        objKEY_AES128 = infoProvider.getKey(Helper.ALIAS_KEY_AES128,
                NfcAppKeys.EnumKeyType.EnumAESKey);
    }

    @Override
    public void onNewIntent(final Intent intent) {
        Log.d(TAG, "onNewIntent");
        try {
            cardLogic(intent);
            super.onNewIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
    // endregion

    // region NFC Logic
    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        type = libInstance.getCardType(intent);
        Log.d(TAG, "Type: " + type);

        switch (type) {
            case DESFireEV1:
                desireEV1CardLogin(DESFireFactory.getInstance().getDESFire(
                        libInstance.getCustomModules()
                ));
                break;
        }
    }

    private void desireEV1CardLogin(IDESFireEV1 desFireEV1) {
        try {
            desFireEV1.getReader().connect();
            desFireEV1.getReader().setTimeout(2000);
            desFireEV1.selectApplication(101);
            Log.d(TAG, "Authentication Start");
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.AES, KeyType.AES128, objKEY_AES128);

            Log.d(TAG, "Authentication Successfull");

            byte[] app_Ids = desFireEV1.getFileIDs();
            Log.d(TAG, "App IDs: " + new String(app_Ids, StandardCharsets.UTF_8) );

            byte[] fileData = desFireEV1.readData(NfcFileType.CUSTOMER_UUID_FILE_ID, 0, 0);
            String y = new String(fileData, StandardCharsets.UTF_8);
            y = y.replaceAll("[^a-zA-Z0-9-]","");
            Log.d(TAG, "App Data 1: " + y);

            byte[] fileData3 = desFireEV1.readData(NfcFileType.CUSTOMER_NAME_FILE_ID, 0, 0);
            Log.d(TAG, "App Data 3: " + new String(fileData3, StandardCharsets.UTF_8));

            byte[] fileData4 = desFireEV1.readData(NfcFileType.CARD_TYPE_FILE_ID, 0, 0);
            String x = new String(fileData4, StandardCharsets.UTF_8);
            x = x.replaceAll("[^a-zA-Z0-9]","");
            Log.d(TAG, "App Data 4: " + x);
        } catch (Exception e) {
            Log.e(TAG, "Auth Fail");
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    // endregion

    // region Permission
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, Helper.RESULT_REQUEST_NFC);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.RESULT_REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Helper.RESULT_REQUEST_NFC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Requested Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "This app was not allowed to use NFC", Toast.LENGTH_SHORT).show();
                }
            case Helper.RESULT_REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Requested Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "This app was not allowed to use External Storage", Toast.LENGTH_SHORT).show();
                }
        }
    }
    // endregion
}
