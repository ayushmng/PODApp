package np.com.bottle.podapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.fragment.EnterPinFragment;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.NeumorphFloatingActionButton;
import soup.neumorphism.ShapeType;

public class EntranceVerificationActivity extends AppCompatActivity {

    CardView cardView;
    NeumorphFloatingActionButton incrementBtn, decreaseBtn;
    NeumorphCardView enterButton;
    TextView numOfVisitors, backButton, buttonText, cardHolderName, cardType, cardNumber, logout;

    String name, number;
    Integer visitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_verificaiton);
        findViewsById();

        name = getIntent().getStringExtra(AdDisplayActivity.Name);
        number = getIntent().getStringExtra(AdDisplayActivity.CardNumber);
        cardHolderName.setText(name);
        cardNumber.setText(number);

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

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                onBackPressed();
            }
        });
    }

    private void findViewsById() {

        cardView = findViewById(R.id.cardView);
        numOfVisitors = findViewById(R.id.tv_visitors);
        incrementBtn = findViewById(R.id.increment_btn);
        decreaseBtn = findViewById(R.id.decrease_btn);
        enterButton = findViewById(R.id.enter_button);
        backButton = findViewById(R.id.back_button);
        buttonText = findViewById(R.id.tv_button);
        cardHolderName = findViewById(R.id.card_holder_name);
        cardType = findViewById(R.id.card_type);
        cardNumber = findViewById(R.id.card_number);
        logout = findViewById(R.id.logout);
    }

    private void setCardDesign(String cardName) {
        switch (cardName) {

            case "diamond":
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.dark_gray));
                break;

            case "platinum":
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                break;
            case "gold":
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.dark_gray));
                break;
        }
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
            } else {
//                showPinCodeDialog();
                //TODO: Save the visitors value in Preference or push to API
                onBackPressed();
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