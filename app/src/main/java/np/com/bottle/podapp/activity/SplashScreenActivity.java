package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.callback.PasswordCallback;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.util.Constants;
import np.com.bottle.podapp.util.Helper;

public class SplashScreenActivity extends AppCompatActivity {

    private static String TAG = SplashScreenActivity.class.getSimpleName();
    private AppPreferences appPref;
    private List<String> appPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        appPermissions = new ArrayList<>();
        appPermissions.add(Manifest.permission.NFC);
        appPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        appPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        appPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        appPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        appPref = new AppPreferences(getApplicationContext());

        if(checkPermission()) {
            ivLogo.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initApp();
                    finish();
                }
            }, 2000);
        }

    }

    public void initApp() {
        if (appPref.getBoolean(AppPreferences.IS_PROVISIONED)) {
            startActivity(new Intent(getApplicationContext(), AdDisplayActivity.class));
        } else {
            startActivity(new Intent(getApplicationContext(), ProvisioningActivity.class));
        }
        finish();
    }

    private boolean checkPermission() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if(!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    Constants.PERMISSIONS_REQUEST_CODE
            );
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "here ------------------ ");
        Log.d(TAG, "permissions: " + permissions.length);
        Log.d(TAG, "grantResults: " + grantResults.length);

        if(requestCode == Constants.PERMISSIONS_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for(int i = 0; i < grantResults.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if(deniedCount == 0) {
                initApp();
            }
            finish();
        }
    }
}
