package np.com.bottle.podapp.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import np.com.bottle.podapp.R;

public class TestingActivity extends AppCompatActivity {

    int[] arr = new int[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        arr[0] = 1;
        arr[1] = 1;
        arr[2] = 1;
    }
}