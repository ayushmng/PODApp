package np.com.bottle.podapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.services.ContentDownloadIntentService;
import np.com.bottle.podapp.util.AlarmReceiver;

public class AlarmActivity extends AppCompatActivity {

    //TimeSlot
    String TAG = "AlarmActivity";
    int timerCount = -1;
    int duration = 1;
    int hr, min;
    List<String> timeSlotList;
    Calendar calendar;
    AlarmManager alarmMgr;
    AlarmManager alarmManager;
    PendingIntent alarmIntent;
    PendingIntent myPendingIntent;
    BroadcastReceiver alarmReceiver;
    BroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        timeSlotList = new ArrayList<>();
        timeSlotList.add("14:24");
        timeSlotList.add("14:26");
        timeSlotList.add("14:28");
        loopTimer();
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
        startAlarm(calendar);
//        registerMyAlarmBroadcast();
//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), myPendingIntent);
    }

    private void loopTimer() {
        timerCount++;
        if (timerCount < timeSlotList.size()) {
            String startingTime = timeSlotList.get(timerCount);
            String time = startingTime.substring(0, 5);

            Log.i(TAG, "Timer slot: " + time);
            setTimeSlot(time);
        }
        Log.i(TAG, "Timer Count: " + timerCount);
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
    protected void onResume() {
        super.onResume();
        registerReceiver(myBroadcastReceiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
    }

}
