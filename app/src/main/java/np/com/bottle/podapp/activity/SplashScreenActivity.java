package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;
import np.com.bottle.podapp.util.Helper;

public class SplashScreenActivity extends AppCompatActivity {

    private static String TAG = SplashScreenActivity.class.getSimpleName();
    private AppPreferences appPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        checkPermission();
        appPref = new AppPreferences(getApplicationContext());

        ivLogo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(appPref.getBoolean(AppPreferences.IS_PROVISIONED)) {
                    startActivity(new Intent(getApplicationContext(), AdDisplayActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(getApplicationContext(), ProvisioningActivity.class));
                    finish();
                }
            }
        }, 2000);
    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, Helper.RESULT_REQUEST_NFC);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.RESULT_REQUEST_EXTERNAL_STORAGE);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_WIFI_STATE}, Helper.REQUEST_CODE_ACCESS_WIFI_STATE);
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
}
