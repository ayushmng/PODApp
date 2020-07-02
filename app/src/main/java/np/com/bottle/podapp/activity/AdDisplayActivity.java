package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.ContentPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.MediaContentAdapter;
import np.com.bottle.podapp.fragment.NfcDetectFragment;
import np.com.bottle.podapp.models.Media;
import np.com.bottle.podapp.nfc.KeyInfoProvider;
import np.com.bottle.podapp.nfc.NfcAppKeys;
import np.com.bottle.podapp.nfc.NfcFileType;
import np.com.bottle.podapp.services.ContentDownloadIntentService;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class AdDisplayActivity extends AppCompatActivity {

    private static String TAG = AdDisplayActivity.class.getSimpleName();
    private AppPreferences appPref;
    private ContentPreferences contentPref;
    PODApp appClass;
    private long longPressTime;

    // NFC
    private NfcAdapter nfcAdapter;
    private NxpNfcLib libInstance;
    private IKeyData objKEY_AES128;

    // AWS
    private AWSIotMqttManager mqttManager;

    // MQTT Topics
    private final String TOPIC_CONTENT_RESPONSE = Constants.TOPIC_CONTENT_RESPONSE;
    private final String TOPIC_CONTENT_REQUEST = Constants.TOPIC_CONTENT_REQUEST;

    private ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private ArrayList<Uri> ImagesArray = new ArrayList<>();
    private List<Media> mediaList;
    private MediaContentAdapter mediaContentAdapter;
    private Timer mediaChangeTimer;
    private int count = 0;
    private String contentDate;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_display);
        context = this;

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        appPref = new AppPreferences(getApplicationContext());
        contentPref = new ContentPreferences(getApplicationContext());
        appClass = (PODApp) getApplication();

        mediaList = new ArrayList<>();

        mPager = findViewById(R.id.vpAdContent);

        checkPermission();
        checkDirectory();
        initializedLibrary();
        initializeKeys();
        initializeMedia();

        mqttManager = appClass.getMqttManager();

        Timer timer = new Timer();
        TimerTask subscribeTask = new TimerTask() {
            @Override
            public void run() {
                if (appClass.isAWSConnected) {
                    subscribeToContentResponse(TOPIC_CONTENT_RESPONSE);
                    publishMsg(TOPIC_CONTENT_REQUEST, "");
                }
            }
        };
        timer.schedule(subscribeTask, 3000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
        registerReceiver(receiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
        mediaLoop(Constants.MEDIALOOPSTATUS.START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
//        ImagesArray.addAll(Arrays.asList(IMAGES));
        Helper.fileCount(getFilesDir().getAbsolutePath() + "/content");
        populateMedia(contentPref.getString(ContentPreferences.CONTENT_DATA));

        Log.d(TAG, "----- content data: " + contentPref.getString(ContentPreferences.CONTENT_DATA));
        Log.d(TAG, "----- content count: " + mediaList.size());

        ImagesArray.add(Uri.parse(getFilesDir().getAbsolutePath() + "/content/ncellimage.jpg"));
        mediaContentAdapter = new MediaContentAdapter(this, ImagesArray, mediaList);
        mPager.setAdapter(mediaContentAdapter);

        // Auto start of viewpager
//        NUM_PAGES = ImagesArray.size();
        NUM_PAGES = mediaList.size();
    }

    private void populateMedia(String contentData) {
        mediaList.clear();

        try {
            JSONObject jPayload = new JSONObject(contentData);
            JSONArray jaData = jPayload.getJSONArray("data");

            contentDate = jPayload.getString("createdAt");

            Log.d(TAG, "data: " + jPayload.getString("data"));
            Log.d(TAG, "array: " + jaData.getJSONObject(0));
            Log.d(TAG, "data array length: " + jaData.length());

            for (int i = 0; i < jaData.length(); i++) {
                JSONArray jaContents = jaData.getJSONObject(i).getJSONArray("contents");

                for (int j = 0; j < jaContents.length(); j++) {
                    JSONObject jContent = jaContents.getJSONObject(j);
                    Log.d(TAG, "Level: " + i + " ---- " + "Name: " + jContent.getString("name"));

                    mediaList.add(new Media(
                            Uri.parse(getFilesDir().getAbsolutePath() + "/content/" + jContent.getString("name") + "." + jContent.getString("extension")),
                            jContent.getInt("interval"),
                            jContent.getString("type"),
                            jaData.getJSONObject(i).getInt("level")
                    ));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing JSON data.");
        } catch (Exception ex) {
            Log.e(TAG, "Error in populating media list.");
        }
    }

    private void checkDirectory() {
        File dir = new File(getFilesDir().getAbsolutePath(), "content");
        if(!dir.exists()) {
            dir.mkdirs();
            Log.d(TAG,"Directory Created");
        } else {
            Log.d(TAG,"Directory Exists");
        }
    }

    /**
     * Method to swipe the viewpager content automatically at the given interval of the contents.
     * @param medialoopstatus enum to start or stop the media loop.
     */
    private void mediaLoop(Constants.MEDIALOOPSTATUS medialoopstatus) {
        switch (medialoopstatus) {
            case START:
                mediaChangeTimer = new Timer();

                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable Update = new Runnable() {
                    public void run() {
                        if (currentPage == NUM_PAGES - 1) {
                            currentPage = 0;
                        } else {
                            currentPage++;
                        }
                        mPager.setCurrentItem(currentPage, true);
                        Log.d(TAG, "currentPage: --- " + currentPage);
                    }
                };

                mediaChangeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        count++;
                        Log.d(TAG, "count: " + count);
                        if (count >= mediaList.get(mPager.getCurrentItem()).Interval) {
                            Log.d(TAG, "current page: " + mPager.getCurrentItem());
                            Log.d(TAG, "current interval: " + mediaList.get(mPager.getCurrentItem()).Interval);

                            handler.post(Update);

                            count = 0;
                        }
                    }
                }, 1000, 1000);
                break;
            case STOP:
                mediaChangeTimer.cancel();
                break;

            default:
                break;
        }
    }

    class DownloadThread extends Thread {
        String strPayload;

        public DownloadThread(String strPayload) {
            this.strPayload = strPayload;
        }

        public void run() {
            try {
                Log.d(TAG, "------------------ Message hereeeee");
                JSONObject payloadData = new JSONObject(strPayload);
                String createdAt = payloadData.getString("createdAt");

                Log.d(TAG, "------------ Date: " + contentDate.equals(createdAt));

//                                if (!Helper.compareDate(createdAt)) {
                if (!Helper.compareDate(contentDate)) {
                    // Saving content to preference
                    contentPref.putString(ContentPreferences.CONTENT_DATA, strPayload);

                    Intent intent = new Intent(AdDisplayActivity.this, ContentDownloadIntentService.class);
                    intent.putExtra(ContentDownloadIntentService.CONTENT_DATA, strPayload);

                    mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
                    startService(intent);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context.getApplicationContext(), R.string.content_status_updated, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }
    }

    // endregion

    // region MQTT Publish & Subscription

    // Publish message to the device_uri topic.
    // QOS 1 is being used for mqtt.
    private void publishMsg(String topic, String payload) {
        mqttManager.publishString(payload, topic, AWSIotMqttQos.QOS1);
        Log.d(TAG, topic + ": Message Sent");
    }

    private void subscribe(String topic) {
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(String topic, byte[] data) {
                            Log.d(TAG, "topic: " + topic);
                            String strData = new String(data, StandardCharsets.UTF_8);
                            Log.d(TAG, "Message: " + strData);

                            switch (topic) {
                                case TOPIC_CONTENT_RESPONSE:
                                    break;
                            }
                        }
                    });
            Log.d(TAG, "Subscribed to topic: " + topic);
        } catch (Exception e) {
            Log.e(TAG, "Error in Subscribing to Topic.");
        }
    }

    private void subscribeToContentResponse(String topic) {
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS1,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(String topic, byte[] data) {

                            Log.d(TAG, "topic: " + topic);
                            String strPayload = new String(data, StandardCharsets.UTF_8);
                            Log.d(TAG, "Message: " + strPayload);

                            DownloadThread downloadThread = new DownloadThread(strPayload);
                            downloadThread.start();
                        }
                    });
            Log.d(TAG, "Subscribed to topic: " + topic);
        } catch (Exception e) {
            Log.e(TAG, "Error in Subscribing to Topic.");
        }
    }


    // endregion

    // region Receiver

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                int resultCode = bundle.getInt(ContentDownloadIntentService.RESULT);

                if(resultCode == ContentDownloadIntentService.RESULT_CODE_SUCCESS) {
                    Toast.makeText(AdDisplayActivity.this, "File downloaded.", Toast.LENGTH_SHORT).show();
                    populateMedia(contentPref.getString(ContentPreferences.CONTENT_DATA));
                    mediaContentAdapter.notifyDataSetChanged();
                    mediaLoop(Constants.MEDIALOOPSTATUS.START);
                } else {
                    Toast.makeText(AdDisplayActivity.this, "File downloaded Error.", Toast.LENGTH_SHORT).show();
                }
            }

            String path = getFilesDir().getAbsolutePath() + "/content";
            File directory = new File(path);
            File[] files = directory.listFiles();
            Log.d(TAG, "Files Count: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "Filename: " + files[i].getName());
            }
        }
    };

    // endregion

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "---------- ACTION_DOWN");
                longPressTime = (Long) System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if(((Long) System.currentTimeMillis() - longPressTime) > 3000){
                    Log.d(TAG, "------------------------------ ACTION_UP");
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

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
