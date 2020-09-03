package np.com.bottle.podapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    public interface MyCustomInterface {
        void sendData(Boolean onFinish);
    }

    private MyCustomInterface myCustomInterface;
    int timerCount = 0;
    List<String> timeSlotList;

    public AlarmReceiver() {
    }

    public void AlarmReceiver(Context context) {
        this.myCustomInterface = (MyCustomInterface) context;
    }

    String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Timer starts");
        /*if (myCustomInterface != null) {
            myCustomInterface.sendData(true);
        }*/
    }

}
