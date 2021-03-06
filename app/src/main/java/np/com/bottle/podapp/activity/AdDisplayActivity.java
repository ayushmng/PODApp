package np.com.bottle.podapp.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.ContentPreferences;
import np.com.bottle.podapp.PODApp;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.MediaContentAdapter;
import np.com.bottle.podapp.fragment.EnterPinFragment;
import np.com.bottle.podapp.fragment.NfcDetectFragment;
import np.com.bottle.podapp.interfaces.OnDialogDismissListener;
import np.com.bottle.podapp.models.Media;
import np.com.bottle.podapp.nfc.KeyInfoProvider;
import np.com.bottle.podapp.nfc.NfcAppKeys;
import np.com.bottle.podapp.nfc.NfcFileType;
import np.com.bottle.podapp.services.ContentDownloadIntentService;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class AdDisplayActivity extends AppCompatActivity implements OnDialogDismissListener {

    private static String TAG = AdDisplayActivity.class.getSimpleName();
    private ConstraintLayout lotteLayout;
    private AppPreferences appPref;
    private ContentPreferences contentPref;
    private PODApp appClass;
    private long longPressTime;
    private Context context;

    //AdView
    private boolean isVideoPlaying = false;
    private String dayOfTheWeek;
    private final static int INTERVAL = 1000 * 10; //10 secs
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
    private static int NUM_PAGES = 0;
    private List<Media> mediaList;

    //TimeSlot
    int[] timeSlotArrayList = new int[24];
    ArrayList<String> daysList = new ArrayList<>();

    private MediaContentAdapter mediaContentAdapter;
    private Timer mediaChangeTimer = new Timer();
    private Timer deviceTimer = new Timer();
    private int count = 0;
    private int tempCount = 0;

    /**
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

        Log.i(TAG, "Is it here?");

        lotteLayout = findViewById(R.id.constraint_lotte);
        appPref = new AppPreferences(getApplicationContext());
        contentPref = new ContentPreferences(getApplicationContext());
        appClass = (PODApp) getApplication();
        mqttManager = appClass.getMqttManager();

        mediaList = new ArrayList<>();
        mPager = findViewById(R.id.vpAdContent);

        //Disables scrolling behaviour
        /*mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });*/

        setToday(true);
        checkDirectory();
        initializedLibrary();
        initializeKeys();
        initializeMedia();

//        lotteLayout.setVisibility(View.VISIBLE);
//        mPager.setVisibility(View.GONE);
//        displayLotteAnim();

        lotteLayout.setVisibility(View.GONE);
        mPager.setVisibility(View.VISIBLE);

        viewPagerChangeListener();
    }

    private void displayLotteAnim() {
        // Helps to display Lotte animation at first for 10sec
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPager.setVisibility(View.VISIBLE);
                        lotteLayout.setVisibility(View.GONE);
                    }
                });
            }
        };
        thread.start();

        // Displays ad view for 1min and animation for 10sec
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                mPager.setVisibility(View.GONE);
                lotteLayout.setVisibility(View.VISIBLE);
                videoPause(true, true);

                handler.postDelayed(this, INTERVAL2);

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mPager.setVisibility(View.VISIBLE);
                                lotteLayout.setVisibility(View.GONE);
                                videoPause(true, false);
                                //TODO: Uncomment below code if DeviceHealthService class has to perform its operation
//                                startService(new Intent(getApplicationContext(), DeviceHealthService.class));
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
        if (Constants.FROM_ENTRANCE_ACTIVITY) {
            videoPause(true, false);
        }
        libInstance.startForeGroundDispatch();
        registerReceiver(receiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(receiver);
        healthTimer.cancel();
        videoPause(true, true);
        deviceTimer.cancel();
        mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
        Log.i(TAG, "Activity Paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        mediaLoop(Constants.MEDIALOOPSTATUS.STOP);
//        videoPause(false, false);
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

        if (type == CardType.DESFireEV1) {
            desireEV1CardLogin(DESFireFactory.getInstance().getDESFire(
                    libInstance.getCustomModules()
            ));

        } else {
            //TODO: Check pausing video works or not
            videoPause(true, true);
            Intent intent1 = new Intent(this, EntranceVerificationActivity.class);
            intent1.putExtra(IsInvalid, true);
            startActivity(intent1);
        }
    }

    private void desireEV1CardLogin(IDESFireEV1 desFireEV1) {
        try {
            desFireEV1.getReader().connect();
            desFireEV1.getReader().setTimeout(2000);
            desFireEV1.selectApplication(101);
            Log.d(TAG, "Authentication Start");
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.AES, KeyType.AES128, objKEY_AES128);

            Log.d(TAG, "Authentication Successful");

            byte[] app_Ids = desFireEV1.getFileIDs();
            Log.d(TAG, "App IDs: " + new String(app_Ids, StandardCharsets.UTF_8));

            byte[] fileData = desFireEV1.readData(NfcFileType.CUSTOMER_UUID_FILE_ID, 0, 0);
            String strUuid = Helper.convertHexToString(fileData);
            Log.d(TAG, "App Data 1: " + strUuid);

            byte[] fileData2 = desFireEV1.readData(NfcFileType.CUSTOMER_NAME_FILE_ID, 0, 0);
            String userName = Helper.convertHexToString(fileData2);
            Log.d(TAG, "App Data 2: " + userName);

            int cardType = desFireEV1.getValue(NfcFileType.CARD_TYPE_FILE_ID);
            Log.d(TAG, "App Data 3: " + cardType);

            int cardNumber = desFireEV1.getValue(NfcFileType.CARD_NUMBER_FILE_ID);
            Log.d(TAG, "App Data 4: " + cardNumber);

            int cardStatus = desFireEV1.getValue(NfcFileType.CARD_STATUS);
            Log.d(TAG, "App Data 5: " + cardStatus);

            int issuedDate = desFireEV1.getValue(NfcFileType.ISSUED_DATE);
            Log.d(TAG, "App Data 6: " + issuedDate);

            int expiryDate = desFireEV1.getValue(NfcFileType.EXPIRY_DATE);
            Log.d(TAG, "App Data 7: " + expiryDate);

            byte[] fileData3 = desFireEV1.readData(NfcFileType.ORGANIZATION_ID, 0, 0);
            String orgId = Helper.convertHexToString(fileData3);
            Log.d(TAG, "App Data 8: " + orgId);

            int balance = desFireEV1.getValue(NfcFileType.BALANCE);
            Log.d(TAG, "App Data 9: " + balance);

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
                //TODO: Check pausing video works or not
                videoPause(true, true);
                Intent intent = new Intent(this, EntranceVerificationActivity.class);
                intent.putExtra(Name, userName);
                intent.putExtra(UserCardNumber, String.valueOf(cardNumber));
                intent.putExtra(UserCardStatus, cardStatus);
                intent.putExtra(UserCardType, cardType);

                //Sending true value if card is expired
                if (Helper.currentDateToEpoch() > expiryDate) {
                    intent.putExtra(IsExpired, true);
                }
                startActivity(intent);

            } else {
                //TODO: Check pausing video works or not
                videoPause(true, true);
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

        addTimeSlots();
        getDeviceTime();
        Log.d(TAG, "----- content data: " + contentPref.getString(ContentPreferences.CONTENT_DATA));
        Log.d(TAG, "----- content count: " + mediaList.size());

        mediaContentAdapter = new MediaContentAdapter(this, mediaList);
        mPager.setAdapter(mediaContentAdapter);
        NUM_PAGES = mediaList.size();

    }

    private void addTimeSlots() {

        try {
            JSONObject jPayload = new JSONObject(contentPref.getString(ContentPreferences.CONTENT_DATA));

            JSONArray jaData = jPayload.getJSONArray("data");
            contentDate = jPayload.getString("createdAt");

            Arrays.fill(timeSlotArrayList, 1);

            for (int i = 0; i < jaData.length(); i++) {

                JSONArray jTimeSlot = jaData.getJSONObject(i).getJSONArray("time_slot");

                for (int k = 0; k < jTimeSlot.length(); k++) {

                    int level = jaData.getJSONObject(i).getInt("level");
                    String time = jTimeSlot.getString(k).substring(0, 2);

                    if (time.substring(1).equals("-")) {
                        time = time.substring(0, time.length() - 1);
                    }

                    Log.d(TAG, "MediaLoop > addTimeSlots - level: " + level);
                    timeSlotArrayList[Integer.parseInt(time)] = level;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void populateMedia(int level) {
        mediaList.clear();

        try {
            JSONObject jPayload = new JSONObject(contentPref.getString(ContentPreferences.CONTENT_DATA));
            JSONArray jaData = jPayload.getJSONArray("data");
            contentDate = jPayload.getString("createdAt");

            Log.d(TAG, "data: " + jPayload.getString("data"));
            Log.d(TAG, "array: " + jaData.getJSONObject(0));
            Log.d(TAG, "data array length: " + jaData.length());

            try {
                //TODO: Changed here because here the level sent and position won't match
                JSONArray jaContents;
                if (level >= 1) {
                    jaContents = jaData.getJSONObject(level - 1).getJSONArray("contents");
                } else {
                    jaContents = jaData.getJSONObject(level).getJSONArray("contents");
                }

                Log.i(TAG, "Day directory: " + dayOfTheWeek);

                for (int j = 0; j < jaContents.length(); j++) {
                    //TODO: Changed here
                    JSONObject jContent = jaContents.getJSONObject(j);
                    Log.d(TAG, "Level: " + level + " ---- " + "Name: " + jContent.getString("name"));

                    Log.i(TAG, "Level data: " + jaData.getJSONObject(level - 1).getInt("level") + "");

                    mediaList.add(new Media(
                            Uri.parse(getFilesDir().getAbsolutePath() + "/content/" + dayOfTheWeek + "/" + jContent.getString("name") + "." + jContent.getString("extension")),
                            jContent.getInt("interval"),
                            jContent.getString("type"),
                            jaData.getJSONObject(level - 1).getInt("level")
                    ));

                }

                NUM_PAGES = mediaList.size();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing JSON data.");
        } catch (Exception ex) {
            Log.e(TAG, "Error in populating media list.");
        }
    }

    /**
     * Adding days list to create directory acc. to same day
     */
    private void checkDirectory() {

        daysList.add("Sunday");
        daysList.add("Monday");
        daysList.add("Tuesday");
        daysList.add("Wednesday");
        daysList.add("Thursday");
        daysList.add("Friday");
        daysList.add("Saturday");

        //Creating directories with 7 days name
        // If directory exists when the app started it goes to else state,
        // otherwise create directory from if state.
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

        //Setting directory path of yesterday's if today's directory got empty
        File directory = new File(getFilesDir().getAbsolutePath(), "content/" + dayOfTheWeek);
        File[] contents = directory.listFiles();
        if (contents.length == 0) {
            Log.i(TAG, "Directory is empty");
            setToday(false);
        } else {
            Log.i(TAG, "Directory is not empty");
            setToday(true);
        }
    }

    /**
     * Helps to pause video from activity rather than adapter class,
     * it is done so to communicate the view pager with media or video
     * <p>
     * Poor communication in adapter about video leads to perform such operations here
     *
     * @param muteAudio as true value works only muting audio and running the video in background
     */
    public void videoPause(boolean playBackground, boolean muteAudio) {
        View myView = mPager.findViewWithTag(mPager.getCurrentItem());

        try {
            PlayerView playerView = myView.findViewById(R.id.pvVideo);
            Player player = playerView.getPlayer();

            if (player != null) {
                if (!playBackground) {
                    playerView.onPause();
                    player.stop();
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                } else {
                    if (muteAudio) {
                        Objects.requireNonNull(player.getAudioComponent()).setVolume(0f);
                    } else {
                        Objects.requireNonNull(player.getAudioComponent()).setVolume(1f);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param isToday sets the String dayOfTheWeek as today's or yesterday's day
     *                if true sets Today's day or Yesterday's day
     */
    private void setToday(boolean isToday) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        if (isToday) {
            Date d = new Date();
            dayOfTheWeek = sdf.format(d);
        } else {
            Date date = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
            dayOfTheWeek = sdf.format(date);
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

                }

                break;
            case STOP:
                mediaChangeTimer.cancel();
                break;

            default:
                break;
        }

    }

    /**
     * Handles the viewpager position of image and video
     * <p>
     * Obtains the extension of the media source as they get swiped manually or automatically,
     * it then helps the video player to play or pause acc. to its own position.
     * This part is done to reduce the bug related with video player, that used to get play before its position.
     */

    private void viewPagerChangeListener() {
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("ScrollState", "OnScrolled");

                position = mPager.getCurrentItem();

                if (mediaList.get(position).MediaType.equals(Media.MEDIA_TYPE_VIDEO)) {
                    View view = mPager.findViewWithTag(position);
                    PlayerView playerView = view.findViewById(R.id.pvVideo);
                    Player player = playerView.getPlayer();

                    if (player != null) {
                        player.addListener(new Player.DefaultEventListener() {
                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                                if (playWhenReady && playbackState == Player.STATE_READY) {
                                    // media actually playing
                                    Log.i(TAG, "Video is playing");
                                } else if (playWhenReady) {
//                                                        videoPause();
                                    Log.i(TAG, "Video is buffering");
                                    // might be idle (plays after prepare()),
                                    // buffering (plays when data available)
                                    // or ended (plays when seek away from end)
                                } else {
                                    Log.i(TAG, "Video is paused");
                                    // player paused in any state
                                }
                            }
                        });
                    }

                }
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
                    }

                }
                Log.i("ScrollState", "OnAutoScrolled");
            }
        });
    }


    /**
     * It helps to obtain the device time and send value to populateMedia();
     */
    public void getDeviceTime() {
        deviceTimer = new Timer();
        deviceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mediaLoop(Constants.MEDIALOOPSTATUS.STOP);

                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                String minute = String.valueOf(calendar.get(Calendar.MINUTE));

                if (minute.length() == 1) {
                    minute = "0" + minute;
                }

                // For debugging purpose
//                int ihour = calendar.get(Calendar.MINUTE);
//                if (ihour > 23) {
//                    ihour = ihour / 3;
//                }
//
//                Log.d(TAG, "MediaLoop > getDeviceTime - ihour: " + ihour);
                Log.d(TAG, "MediaLoop > getDeviceTime - hour: " + hour);
                Log.d(TAG, "MediaLoop > getDeviceTime - timeslotarraylist value: " + timeSlotArrayList[Integer.parseInt(hour)]);
                Log.d(TAG, "MediaLoop > getDeviceTime - timeslotarraylist: " + Arrays.toString(timeSlotArrayList));


                populateMedia(timeSlotArrayList[Integer.parseInt(hour)]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaContentAdapter.notifyDataSetChanged();
                    }
                });

                mediaLoop(Constants.MEDIALOOPSTATUS.START);
            }
        }, 1000, 1000 * 60);

    }

    @Override
    public void dialogDismissed(Boolean dismissed) {
        videoPause(true, false);
    }

    /**
     * Checks if content is already downloaded or not
     */

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

                if (!Helper.compareDate(contentDate)) {

                    // Saving content to preference
                    contentPref.putString(ContentPreferences.CONTENT_DATA, strPayload);

                    Intent intent = new Intent(AdDisplayActivity.this, ContentDownloadIntentService.class);
                    intent.putExtra(ContentDownloadIntentService.CONTENT_DATA, strPayload);

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

    /**
     * Publish message to the device_uri topic.
     * QOS 1 is being used for mqtt.
     */
    private void publishMsg(String topic, String payload) {
        mqttManager.publishString(payload, topic, AWSIotMqttQos.QOS1);
        Log.d(TAG, topic + ": Message Sent");
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
                    setToday(true); // Setting day as same day if the file gets download


                    // This block is used to reset the timeslot timer and media loop.
                    Thread getDeviceTimeThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            addTimeSlots();
                        }
                    });
                    getDeviceTimeThread.start();

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
    public boolean dispatchTouchEvent(@NotNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "---------- ACTION_DOWN");
                longPressTime = (Long) System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (((Long) System.currentTimeMillis() - longPressTime) > 3000) {
                    Log.d(TAG, "------------------------------ ACTION_UP");
                    //TODO: Check pausing video works or not
                    videoPause(true, true);
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

        Constants.FROM_ENTRANCE_ACTIVITY = false;
        EnterPinFragment dialogFragment = new EnterPinFragment(value, this);
        dialogFragment.setArguments(args);
        dialogFragment.show(ft, "dialog");

        Log.i(TAG, "Is it here?");
    }

    public static String Name = "NAME";
    public static String UserCardNumber = "CARD_NUMBER";
    public static String UserCardType = "CARD_TYPE";
    public static String UserCardStatus = "CARD_STATUS";
    public static String IsInvalid = "INVALID";
    public static String IsExpired = "EXPIRED";
}
