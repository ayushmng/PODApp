package np.com.bottle.podapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import np.com.bottle.podapp.R;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.NeumorphFloatingActionButton;
import soup.neumorphism.ShapeType;

public class EntranceVerificaitonActivity extends AppCompatActivity {

    NeumorphFloatingActionButton incrementBtn, decreaseBtn;
    NeumorphCardView enterButton;
    TextView numOfVisitors, backButton, buttonText;

    Integer visitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_verificaiton);
        findViewsById();

        visitors = Integer.valueOf(numOfVisitors.getText().toString());
        if (visitors == 3) {
            decreaseBtn.setShapeType(ShapeType.DEFAULT);
            incrementBtn.setShapeType(ShapeType.PRESSED);
            incrementBtn.setClickable(false);
        } else if (visitors == 1) {
            incrementBtn.setShapeType(ShapeType.DEFAULT);
            decreaseBtn.setClickable(false);
            decreaseBtn.setShapeType(ShapeType.PRESSED);
        }

        incrementBtn.setOnClickListener(incrementClickListener);
        decreaseBtn.setOnClickListener(decrementClickListener);
        enterButton.setOnClickListener(enterButtonClickListener);
        backButton.setOnClickListener(backButtonClickListener);
    }

    private void findViewsById() {
        numOfVisitors = findViewById(R.id.tv_visitors);
        incrementBtn = findViewById(R.id.increment_btn);
        decreaseBtn = findViewById(R.id.decrease_btn);
        enterButton = findViewById(R.id.enter_button);
        backButton = findViewById(R.id.back_button);
        buttonText = findViewById(R.id.tv_button);
    }

    private View.OnClickListener incrementClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            if (visitors <= 3 && visitors >= 1) {
                decreaseBtn.setShapeType(ShapeType.DEFAULT);
            }

            if (visitors < 3) {
                incrementBtn.setShapeType(ShapeType.DEFAULT);
                visitors++;
                numOfVisitors.setText(visitors.toString());
            } else {
                decreaseBtn.setShapeType(ShapeType.DEFAULT);
                incrementBtn.setShapeType(ShapeType.PRESSED);
            }
        }
    };

    private View.OnClickListener decrementClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {

            if (visitors <= 3 && visitors >= 1) {
                incrementBtn.setShapeType(ShapeType.DEFAULT);
            }

            if (visitors > 1) {
                decreaseBtn.setShapeType(ShapeType.DEFAULT);
                visitors--;
                numOfVisitors.setText(visitors.toString());
            } else {
                incrementBtn.setShapeType(ShapeType.DEFAULT);
                decreaseBtn.setShapeType(ShapeType.PRESSED);
            }
        }
    };

    private View.OnClickListener enterButtonClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            if (buttonText.getText().toString().toUpperCase().equals("ENTER")) {

                incrementBtn.setVisibility(View.GONE);
                decreaseBtn.setVisibility(View.GONE);

                numOfVisitors.setTextSize(TypedValue.COMPLEX_UNIT_SP, 92);
                numOfVisitors.setTextColor(getResources().getColor(R.color.red));

                buttonText.setText("CONFIRM");
                backButton.setVisibility(View.VISIBLE);
            }
        }
    };

    private View.OnClickListener backButtonClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            incrementBtn.setVisibility(View.VISIBLE);
            decreaseBtn.setVisibility(View.VISIBLE);

            numOfVisitors.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
            numOfVisitors.setTextColor(getResources().getColor(R.color.dark_gray));

            buttonText.setText("ENTER");
            backButton.setVisibility(View.GONE);
        }
    };
}