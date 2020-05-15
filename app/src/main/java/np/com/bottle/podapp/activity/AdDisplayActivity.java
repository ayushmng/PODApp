package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.MediaContentAdapter;
import np.com.bottle.podapp.fragment.NfcDetectFragment;
import np.com.bottle.podapp.nfc.KeyInfoProvider;
import np.com.bottle.podapp.nfc.NfcAppKeys;
import np.com.bottle.podapp.nfc.NfcFileType;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class AdDisplayActivity extends AppCompatActivity {

    private static String TAG = AdDisplayActivity.class.getSimpleName();
    private AppPreferences appPref;
    PODApp appClass;

    // NFC
    private NfcAdapter nfcAdapter;
    private NxpNfcLib libInstance;
    private IKeyData objKEY_AES128;

    // AWS
    private AWSIotMqttManager mqttManager;

    // Media Content
    private ViewPager mPager;
    private ImageView mImageView;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private ArrayList<Uri> ImagesArray = new ArrayList<>();
    private Uri[] IMAGES = {
            Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/car1.jpg"),
            Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/car2.jpeg"),
            Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/car3.jpeg")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_display);

        appPref = new AppPreferences(getApplicationContext());
        appClass = (PODApp) getApplication();

        mPager = findViewById(R.id.vpAdContent);

        checkPermission();
        initializedLibrary();
        initializeKeys();
        initializeMedia();

        mqttManager = appClass.getMqttManager();

//        if(appClass.getAWSStatus()) {
//            publishMsg("Hello from android!!!!!!");
//            subscribe(appPref.getString(AppPreferences.DEVICE_URI));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    // Publish message to the device_uri topic.
    // QOS 1 is being used for mqtt.
    private void publishMsg(String topic, String payload) {
//        String topic = appPref.getString(AppPreferences.DEVICE_URI);
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
            String strUuid = Helper.convertHexToString(fileData);
            Log.d(TAG, "App Data 1: " + strUuid);

            int strCardNumber = desFireEV1.getValue(NfcFileType.CARD_NUMBER_FILE_ID);
            Log.d(TAG, "App Data 2: " + strCardNumber);

            byte[] fileData3 = desFireEV1.readData(NfcFileType.CUSTOMER_NAME_FILE_ID, 0, 0);
            String strName = Helper.convertHexToString(fileData3);
            Log.d(TAG, "App Data 3: " + strName);

            byte[] fileData4 = desFireEV1.readData(NfcFileType.CARD_TYPE_FILE_ID, 0, 0);
            Log.d(TAG, "App Data 4: " + Helper.convertHexToString(fileData4));


            // Creating Payload for mqtt publish
            JSONObject paymentPayload = new JSONObject();
            paymentPayload.put("uuid", strUuid);
            paymentPayload.put("deviceid", appPref.getString(AppPreferences.DEVICE_ID));
            paymentPayload.put("fleetid", appPref.getString(AppPreferences.FLEET_ID));
            paymentPayload.put("timeStamp", "");
            paymentPayload.put("type", "payment");
            String payload = paymentPayload.toString();
            Log.d(TAG, "paymentPayload: " + payload);

            publishMsg(Constants.TOPIC_NFC_PAYMENT, payload);
            showPaymentDialog(strName, strCardNumber);
        } catch (Exception e) {
            Log.e(TAG, "Auth Fail");
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void showPaymentDialog(String name, int cardNumber) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("cardNumber", cardNumber);

        DialogFragment dialogFragment = new NfcDetectFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(ft, "dialog");
    }

    // endregion

    // region Content Media
    private void initializeMedia() {
        ImagesArray.addAll(Arrays.asList(IMAGES));

        Log.d(TAG, "ImagesArray Size: " + ImagesArray.size());
        mPager.setAdapter(new MediaContentAdapter(this, ImagesArray));

        // Auto start of viewpager
        NUM_PAGES = ImagesArray.size();

        MediaThread mediaThread = new MediaThread(mPager);
        mediaThread.start();
    }

    /**
     * Thread for updating the ViewPager according to the image or video interval.
     */
    private static class MediaThread extends Thread {
        int count;
        ViewPager pager;
        MediaThread(ViewPager pager) {
            count = 0;
            this.pager = pager;
        }

        public void run() {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    count++;
                }
            }, 1000, 1000);

            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable Update = new Runnable() {
                public void run() {
                    pager.setCurrentItem(currentPage++, true);
                }
            };

            try {
                while (true) {
                    if (count >= 2) {
                        if (currentPage == NUM_PAGES) {
                            currentPage = 0;
                        }

                        handler.post(Update);

                        count = 0;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Thread Error: " + e.getMessage());
            }
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
