package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.interfaces.WebsocketListener;
import np.com.bottle.podapp.util.Helper;
import np.com.bottle.podapp.util.WebSocketHelper;

public class ProvisioningActivity extends AppCompatActivity implements WebsocketListener {

    private static String TAG = ProvisioningActivity.class.getSimpleName();
    private final int REQUEST_CODE_ACCESS_WIFI_STATE = 1;

    private WebSocketHelper wSocket;
    private String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning);

        checkPermission();

        ipAddress = Helper.getIpAddress(getApplicationContext());

        int port = 8000;
        try {
            wSocket = new WebSocketHelper(this, port);
            wSocket.start();
            Log.d(TAG, "ChatServer started on port: " + wSocket.getPort());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    @Override
    public void onAwsConfigReceived() {
        Log.d(TAG, "onAwsConfigReceived");
    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_CODE_ACCESS_WIFI_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_WIFI_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "WiFi Access State Permission Granted.");
                } else {
                    Log.e(TAG, "WiFi Access State Permission Not Granted");
                }
        }
    }
}
