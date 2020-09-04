package np.com.bottle.podapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.services.ContentDownloadIntentService;
import np.com.bottle.podapp.util.AlarmReceiver;

public class AlarmActivity extends AppCompatActivity implements AlarmReceiver.MyCustomInterface {

    //TimeSlot
    String TAG = "AlarmActivity";
    int timerCount = -1;
    int duration = 1;
    int hr, min;
    List<String> timeSlotList;
    Calendar calendar;
    AlarmManager alarmMgr;
    AlarmManager alarmManager;
    AlarmReceiver receiver;
    PendingIntent alarmIntent;
    PendingIntent myPendingIntent;
    BroadcastReceiver alarmReceiver;
    BroadcastReceiver myBroadcastReceiver;

    //Device Health Test
    String downlink, uplink;
    long mStartRX;
    long mStartTX;

    private SensorManager mSensorManager;
    private Sensor mTemperature;
    private final static String NOT_SUPPORTED_MESSAGE = "Sorry, sensor not available for this device.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        receiver = new AlarmReceiver();
        receiver.AlarmReceiver(this);

        mStartRX = TrafficStats.getTotalRxBytes();
        mStartTX = TrafficStats.getTotalTxBytes();

        timeSlotList = new ArrayList<>();
        timeSlotList.add("16:18");
        timeSlotList.add("16:20");
        timeSlotList.add("16:21");
        loopTimer();

        if (mStartRX != TrafficStats.UNSUPPORTED || mStartTX != TrafficStats.UNSUPPORTED) {

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {

                    long rxBytes = (TrafficStats.getTotalRxBytes() - mStartRX) / 1024;
                    downlink = Long.toString(rxBytes);

                    if (downlink.length() >= 4) {
                        float mbps = (float) rxBytes / 1024;
                        downlink = (String.valueOf(mbps)).substring(0, 4) + "mbps";
                    } else {
                        downlink = downlink + "kbps";
                    }

                    long txBytes = (TrafficStats.getTotalTxBytes() - mStartTX) / 1024;
                    uplink = Long.toString(txBytes);

                    if (uplink.length() >= 4) {
                        float mbps = (float) txBytes / 1024;
                        uplink = (String.valueOf(mbps)).substring(0, 4) + "mbps";
                    } else {
                        uplink = uplink + "kbps";
                    }

                    mStartRX = TrafficStats.getTotalRxBytes();
                    mStartTX = TrafficStats.getTotalTxBytes();

                    Log.i(TAG, "UploadSpeed: " + uplink + " DownloadSpeed: " + downlink);
                    Log.i(TAG, "Temperature: " + cpuTemperature());

                }
            };
            timer.schedule(timerTask, 1000, 1000);
        }

    }

    public static float cpuTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            } else {
                return 51.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    private void startAlarm(Calendar c) {
        Log.i(TAG, "Timer is on");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void setTimeSlot(String time) {

        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, hr);
//        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5)));
        calendar.set(Calendar.SECOND, 0);
//        startAlarm(calendar);
        registerMyAlarmBroadcast();
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), myPendingIntent);
    }

    private void loopTimer() {
        timerCount++;
        if (timerCount < timeSlotList.size()) {
            String startingTime = timeSlotList.get(timerCount);
            String time = startingTime.substring(0, 5);

            Log.i(TAG, "Timer slot: " + time);
            setTimeSlot(time);
        }
    }

    private void registerMyAlarmBroadcast() {
        myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "BroadcastReceiver::OnReceive()");
                int dur = (1000 * 60 * duration);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Timer Started");
                        loopTimer();
                    }
                }, dur);

                /*new CountDownTimer((1000 * 60 * duration), (1000 * 60 * duration)) {
                    public void onTick(long millisUntilFinished) {
                        Log.i(TAG, "Timer Starts : " + millisUntilFinished);
                    }

                    public void onFinish() {
                        Log.i(TAG, "Timer Completed");
                        timerCount++;
                        if (timerCount < timeSlotList.size()) {
                            String startingTime = timeSlotList.get(timerCount);
                            String time = startingTime.substring(0, 5);
                            Log.i(TAG, "Next Timer : " + time);
                            setTimeSlot(time);
                        }
                    }
                }.start();*/

            }
        };

        registerReceiver(myBroadcastReceiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
        myPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ContentDownloadIntentService.NOTIFICATION), 0);
        alarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }

    private void registerMyAlarmBroadcast(int duration) {
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
                            setTimeSlot(time);
                        }
                    }
                }.start();
            }
        };
    }

    @Override
    public void sendData(Boolean onFinish) {
        Log.i(TAG, "Here is boolean: " + onFinish);
        Toast.makeText(this, "Boolean: " + onFinish, Toast.LENGTH_SHORT).show();
    }

}
