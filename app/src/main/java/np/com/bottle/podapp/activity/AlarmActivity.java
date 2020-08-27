package np.com.bottle.podapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.util.AlarmReceiver;
import np.com.bottle.podapp.util.Constants;

public class AlarmActivity extends AppCompatActivity implements AlarmReceiver.MyCustomInterface {

    //TimeSlot
    String TAG = "AlarmActivity";
    int timerCount = -1;
    int duration = 1;
    int hr, min;
    List<String> timeSlotList;
    Calendar calendar;
    AlarmManager alarmMgr;
    AlarmReceiver receiver;
    PendingIntent alarmIntent;
    BroadcastReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        receiver = new AlarmReceiver();
        receiver.AlarmReceiver(this);
        timeSlotList = new ArrayList<>();
        timeSlotList.add("11:29 - 15:53");
        timeSlotList.add("11:31 - 15:47");
        timeSlotList.add("11:33 - 15:50");
        loopTimer();
        if (Constants.IS_START) {
            loopTimer();
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
        startAlarm(calendar);
    }

    private void loopTimer() {
        timerCount++;
        if (timerCount < timeSlotList.size()) {
            String startingTime = timeSlotList.get(timerCount);
            String time = startingTime.substring(0, 5);
            Log.i(TAG, "Time duration: " + time);

            setTimeSlot(time);
        }
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

    public void countDownTimer() {
        if (Constants.IS_START) {
            timeSlotList = new ArrayList<>();
            new CountDownTimer((1000 * 60 * duration), (1000 * 60 * duration) / 2) {
                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "Timer Starts");
                }

                public void onFinish() {
                    Log.i(TAG, "Timer Completed");
                    loopTimer();
                }
            }.start();
        }
    }

    @Override
    public void sendData(Boolean onFinish) {
        Log.i(TAG, "Here is boolean: " + onFinish);
        Toast.makeText(this, "Boolean: " + onFinish, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(BroadcastReceiver broadcastReceiver, new IntentFilter(ContentDownloadIntentService.NOTIFICATION));
    }
}
