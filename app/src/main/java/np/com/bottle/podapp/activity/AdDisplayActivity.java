package np.com.bottle.podapp.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.interfaces.IKeyData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.ContentPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.MediaContentAdapter;
import np.com.bottle.podapp.fragment.EnterPinFragment;
import np.com.bottle.podapp.fragment.NfcDetectFragment;
import np.com.bottle.podapp.models.Media;
import np.com.bottle.podapp.nfc.KeyInfoProvider;
import np.com.bottle.podapp.nfc.NfcAppKeys;
import np.com.bottle.podapp.nfc.NfcFileType;
import np.com.bottle.podapp.services.ContentDownloadIntentService;
import np.com.bottle.podapp.services.DeviceHealthService;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class AdDisplayActivity extends AppCompatActivity implements MediaContentAdapter.OnVideoEndListener {

    private static String TAG = AdDisplayActivity.class.getSimpleName();
    private ConstraintLayout lotteLayout;
    private AppPreferences appPref;
    private ContentPreferences contentPref;
    private PODApp appClass;
    private long longPressTime;
    private Context context;

    //TimeSlot
    Calendar calendar;
    AlarmManager alarmMgr;
    PendingIntent alarmIntent;
    BroadcastReceiver alarmReceiver;

    //AdView
    private boolean isVideoPlaying = false;
    private String dayOfTheWeek;
    private final static int INTERVAL = 1000 * 5; //10 secs
    private final static int INTERVAL2 = 1000 * 60; //1 min

    // NFC
    private NfcAdapter nfcAdapter;
    private NxpNfcLib libInstance;
    private IKeyData objKEY_AES128;

    // AWS
    private AWSIotMqttManager mqttManager;

    // MQTT Topics
    private final String TOPIC_CONTENT_RESPONSE = Constants.TOPIC_CONTENT_SUB;
    private final String TOPIC_CONTENT_REQUEST = Constants.TOPIC_CONTENT_PUB;

    private ViewPager mPager;
    private static int currentPage;
    //    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private List<Media> mediaList;

    private List<String> timeSlotList;
    int timerCount = -1;

    private MediaContentAdapter mediaContentAdapter;
    private Timer mediaChangeTimer = new Timer();
    private int count = 0;
    private int tempCount = 0;

    /*
     * Initialize content date so as to not make it null.
     * Initial value of contentDate is random date. It has no use.
     */
    private String contentDate = "1970-1-1";

    // Device Health
    Timer healthTimer = new Timer();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_display);
        context = this;
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        lotteLayout = findViewById(R.id.constraint_lotte);
        appPref = new AppPreferences(getApplicationContext());
        contentPref = new ContentPreferences(getApplicationContext());
        appClass = (PODApp) getApplication();
        mqttManager = appClass.getMqttManager();

        mediaList = new ArrayList<>();
        timeSlotList = new ArrayList<>();

        mPager = findViewById(R.id.vpAdContent);

        //Disables scrolling behaviour
        /*mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });*/

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        dayOfTheWeek = sdf.format(d);

        checkDirectory();
        initializedLibrary();
        initializeKeys();
        initializeMedia();
//        deviceMetrics();
//        setTimeSlot(10, 58);

        lotteLayout.setVisibility(View.GONE);
        mPager.setVisibility(View.VISIBLE);
//        handleAdViews();
    }

    private void setTimeSlot(String time, int duration) {

        // Set the alarm to start at provided time frame
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5)));
        calendar.set(Calendar.MINUTE, 0);

        registerMyAlarmBroadcast(duration);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_DAY, alarmIntent);

//        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    private void registerMyAlarmBroadcast(int duration) {
        //Calls when alarm time reached
        alarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new CountDownTimer((1000 * 60 * duration), (1000 * 60 * duration) / 2) {
                    public void onTick(long millisUntilFinished) {
                        Log.i(TAG, "Timer Starts : " + millisUntilFinished);
                    }

                    public void onFinish() {
                        Log.i(TAG, "Timer Completed");
                        timerCount++;
                        if (timerCount < timeSlotList.size()) {
                            String startingTime = timeSlotList.get(timerCount);
                            String time = startingTime.substring(0, 5);
                            Log.i(TAG, "Timer duration: " + time);
                            setTimeSlot(time, 1);
                        }
                    }
                }.start();
            }
        };

        registerReceiver(alarmReceiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(ContentDownloadIntentService.NOTIFICATION), 0);
        alarmMgr = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }

    private void UnregisterAlarmBroadcast() {
        alarmMgr.cancel(alarmIntent);
        getBaseContext().unregisterReceiver(alarmReceiver);
    }

    private void handleAdViews() {
        // Helps to display Lotte animation at first for 10sec

            /*Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPager.setVisibility(View.VISIBLE);
                            lotteLayout.setVisibility(View.GONE);
                            mediaLoop(Constants.MEDIALOOPSTATUS.START);
                        }
                    });
                }
            };
            thread.start();*/

        // Displays ad view for 1min and animation for 10sec
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

//                mPager.setVisibility(View.GONE);
//                lotteLayout.setVisibility(View.VISIBLE);

                Log.i("Show Timer", "Timer starts");
                handler.postDelayed(this, INTERVAL2);

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Show Timer", "Thread starts");
                                /*mPager.setVisibility(View.VISIBLE);
                                lotteLayout.setVisibility(View.GONE);
                                mediaLoop(Constants.MEDIALOOPSTATUS.START);*/
                                startService(new Intent(getApplicationContext(), DeviceHealthService.class));
//                                mPager.setCurrentItem(currentPage, true);
                            }
                        });
                    }
                };
                thread.start();
            }
        }, INTERVAL2);
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
        healthTimer.cancel();
        videoPause();
        Log.i(TAG, "Activity Paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
        videoPause();
        Log.i(TAG, "Activity Destroyed");
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
        infoProvider.setKey(Constants.ALIAS_KEY_AES128, NfcAppKeys.EnumKeyType.EnumAESKey, NfcAppKeys.KEY_AES128_AllAccess);

        KeyData keyDataObj = new KeyData();
        objKEY_AES128 = infoProvider.getKey(Constants.ALIAS_KEY_AES128,
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

            //TODO: Check invalid condition here:
            default:
                Constants.IS_CARD_INVALID = true;
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
            Log.d(TAG, "App IDs: " + new String(app_Ids, StandardCharsets.UTF_8));

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

            //TODO: Check out
//            publishMsg(Constants.TOPIC_NFC_PAYMENT_PUB, payload);

            //TODO: Check this line
//            showPaymentDialog(strName, strCardNumber);

            if (Constants.IS_ENTRANCE_VERIFICATION) {
                Intent intent = new Intent(this, EntranceVerificationActivity.class);
                intent.putExtra(Name, strName);
                intent.putExtra(CardNumber, strCardNumber);
                startActivity(intent);
            } else {
                showPinCodeDialog(1);
            }


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
        // Subscription to Ad Content topic.
        // Initial request for today's Ad Contents.
        // Runs only once on activity start.
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

        // Only for debugging purpose.
        Helper.fileCount(getFilesDir().getAbsolutePath() + "/content");

        populateMedia(contentPref.getString(ContentPreferences.CONTENT_DATA));

        Log.d(TAG, "----- content data: " + contentPref.getString(ContentPreferences.CONTENT_DATA));
        Log.d(TAG, "----- content count: " + mediaList.size());

        mediaContentAdapter = new MediaContentAdapter(this, AdDisplayActivity.this, mediaList);
        mPager.setAdapter(mediaContentAdapter);
        NUM_PAGES = mediaList.size();

        //------------------------------------------------------------//

        timeSlotList.add("16:47 - 15:53");
        timeSlotList.add("16:49 - 15:47");
        timeSlotList.add("16:51 - 15:50");

        timerCount++;
        if (timerCount < timeSlotList.size()) {
            String startingTime = timeSlotList.get(timerCount);
            String time = startingTime.substring(0, 5);
            Log.i(TAG, "Timer duration: " + time);
            setTimeSlot(time, 1);
        }

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
                JSONArray jTimeSlot = jaData.getJSONObject(i).getJSONArray("time_slot");

                for (int j = 0; j < jaContents.length(); j++) {
                    JSONObject jContent = jaContents.getJSONObject(j);
                    Log.d(TAG, "Level: " + i + " ---- " + "Name: " + jContent.getString("name"));

                    mediaList.add(new Media(
                            Uri.parse(getFilesDir().getAbsolutePath() + "/content/" + dayOfTheWeek + "/" + jContent.getString("name") + "." + jContent.getString("extension")),
                            jContent.getInt("interval"),
                            jContent.getString("type"),
                            jaData.getJSONObject(i).getInt("level")
                    ));
                }

                //Adds timeSlot from JSON data
                /*for (int k = 0; k < jTimeSlot.length(); k++) {
                    timeSlotList.add(jTimeSlot.getString(k));
                }*/
            }

            /*String startingTime = timeSlotList.get(0);
//            int duration = startingTime.charAt(timeSlotList.size() - 1) - startingTime.charAt(0);
//            Log.i(TAG, "Duration: " + duration);
//            setTimeSlot(Integer.parseInt(startingTime), duration);

            int time = Integer.parseInt(startingTime.substring(1, 5));
            Log.i(TAG, "Time duration: " + time);
            setTimeSlot(time, 1);

            String startingTime = timeSlotList.get(0);
            String time = startingTime.substring(0, 5);
            Log.i(TAG, "Time duration: " + time);
            setTimeSlot(time, 1);*/

        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing JSON data.");
        } catch (Exception ex) {
            Log.e(TAG, "Error in populating media list.");
        }
    }

    private void checkDirectory() {

        ArrayList<String> daysList = new ArrayList<>();
        daysList.add("Sunday");
        daysList.add("Monday");
        daysList.add("Tuesday");
        daysList.add("Wednesday");
        daysList.add("Thursday");
        daysList.add("Friday");
        daysList.add("Saturday");

        //Creating directories with 7 days name
        File dir;
        for (int i = 0; i < 7; i++) {
            dir = new File(getFilesDir().getAbsolutePath(), "content/" + daysList.get(i));
            if (!dir.exists()) {
                dir.mkdirs();
                Log.d(TAG, "Directory Created");
            } else {
                Log.d(TAG, "Directory Exists");
            }
        }

        // Clearing other days directory media items
        File dirToDelete;
        for (String dayDirectory : daysList) {
            if (!dayDirectory.equals(dayOfTheWeek)) {
                dirToDelete = new File(getFilesDir().getAbsolutePath(), "content/" + dayDirectory);
                if (dirToDelete.isDirectory()) {
                    String[] children = dirToDelete.list();
                    assert children != null;
                    for (int i = 0; i < children.length; i++) {
                        new File(dirToDelete, children[i]).delete();
                        Log.i("File Name", children[i]);
                    }
                }
            }
        }
    }

    public void videoPause() {
        View myView = mPager.findViewWithTag(mPager.getCurrentItem());
        PlayerView playerView = myView.findViewById(R.id.pvVideo);
        Player player = playerView.getPlayer();
        if (player != null) {
            playerView.onPause();
            player.stop();
            player.seekTo(0);
            player.setPlayWhenReady(false);
        }
    }

    /**
     * Method to swipe the viewpager content automatically at the given interval of the contents.
     *
     * @param medialoopstatus enum to start or stop the media loop.
     */
    private void mediaLoop(Constants.MEDIALOOPSTATUS medialoopstatus) {
        switch (medialoopstatus) {
            case START:
                if (NUM_PAGES > 0) {
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

                            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                    Log.i("ScrollState", "OnScrolled");
                                    Log.i("ScrollState", mediaList.get(position).MediaType);
                                }

                                @Override
                                public void onPageSelected(int position) {
                                    Log.i("ScrollState", "OnPageSelected");
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {
                                    // Allows video player to play at its specific position
                                    state = mPager.getCurrentItem();
                                    if (mediaList.get(state).MediaType.equals(Media.MEDIA_TYPE_VIDEO)) {
                                        View view = mPager.findViewWithTag(state);
                                        PlayerView playerView = view.findViewById(R.id.pvVideo);
                                        Player player = playerView.getPlayer();
                                        if (player != null) {
                                            playerView.setPlayer(player);
                                            player.setPlayWhenReady(true);
                                            Log.i("ScrollState", "Player ready");
                                        } else {
                                            Log.i("ScrollState", "Player not ready");
                                        }
                                    }

                                    Log.i("ScrollState", "OnAutoScrolled");
                                }
                            });
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

                                if (!isVideoPlaying) {
                                    handler.post(Update);
                                    count = 0;
                                }
                            }
                        }
                    }, 1000, 1000);
                }
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
                Log.d(TAG, "NUM_PAGES: " + NUM_PAGES);
                JSONObject payloadData = new JSONObject(strPayload);
                String createdAt = payloadData.getString("createdAt");

                if (!Helper.compareDate(contentDate) || NUM_PAGES == 0) {
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
                Log.e(TAG, "JSONException: " + e.getMessage());
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
//                            switch (topic) {
//                                case TOPIC_CONTENT_RESPONSE:
//                                    break;
//                            }
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
            if (bundle != null) {
                int resultCode = bundle.getInt(ContentDownloadIntentService.RESULT);

                if (resultCode == ContentDownloadIntentService.RESULT_CODE_SUCCESS) {
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

    // Device Health
    public void deviceMetrics() {
        // Todo: Device metrics [CPU usage, temperature, RAM usage] need to be implemented.
        TimerTask healthTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String payload = Helper.generateMqttDeviceHealthPayload(
                            appPref.getString(AppPreferences.DEVICE_ID),
                            Constants.PAYLOAD_TYPE_HEALTH,
                            appPref.getString(AppPreferences.FLEET_ID),
                            Integer.toString(new Random().nextInt(956) + 128) + "mb", // Dummy value
                            Integer.toString(new Random().nextInt(85) + 15) + "%", // Dummy value
                            Integer.toString(new Random().nextInt(45)) + "c", // Dummy value. Float.toString(Helper.getCurrentCPUTemperatureInCelcius())
                            Integer.toString(wifiInfo.getRssi()),
                            Integer.toString(new Random().nextInt(5) + 1) + "mbps", // Dummy value
                            Integer.toString(new Random().nextInt(10) + 1) + "mbps", // Dummy value
                            "",
                            ""
                    );
                    Log.d(TAG, "Health: " + payload);
                    Log.d(TAG, "temp: " + Helper.getCurrentCPUTemperatureInCelcius());
                    if (appClass.isAWSConnected) {
                        publishMsg(Constants.TOPIC_TELEMETRY_PUB, payload);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Health data error: " + e.getMessage());
                }
            }
        };

        healthTimer.schedule(healthTimerTask, 1000, 15000);

    }

    @Override
    public boolean dispatchTouchEvent(@NotNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "---------- ACTION_DOWN");
                longPressTime = (Long) System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (((Long) System.currentTimeMillis() - longPressTime) > 3000) {
                    Log.d(TAG, "------------------------------ ACTION_UP");
                    showPinCodeDialog(0);
//                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Note: int value 1 -> takes to Payment Processing
     * whereas, value 0 -> takes to Settings Activity
     */

    // Displays PinCode Dialog box
    private void showPinCodeDialog(int value) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
//        args.putString("name", name);
//        args.putInt("cardNumber", cardNumber);

        EnterPinFragment dialogFragment = new EnterPinFragment(value);
        dialogFragment.setArguments(args);
        dialogFragment.show(ft, "dialog");
    }

    public static String Name = "NAME";
    public static String CardNumber = "CARD_NUMBER";
}
