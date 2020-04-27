package np.com.bottle.podapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static String TAG = SplashScreenActivity.class.getSimpleName();
    private AppPreferences appPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView ivLogo = findViewById(R.id.ivLogo);

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
}
