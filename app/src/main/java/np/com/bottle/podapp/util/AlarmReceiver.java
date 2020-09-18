package np.com.bottle.podapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import np.com.bottle.podapp.activity.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {

    List<String> timeSlotList;
    String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Timer starts");

        Intent scheduleIntent = new Intent(context, AlarmActivity.class);
        scheduleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(scheduleIntent);
    }

}
