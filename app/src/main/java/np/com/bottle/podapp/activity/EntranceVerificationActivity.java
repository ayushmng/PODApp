package np.com.bottle.podapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import np.com.bottle.podapp.R;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.NeumorphFloatingActionButton;
import soup.neumorphism.ShapeType;

public class EntranceVerificationActivity extends AppCompatActivity {

    ConstraintLayout invalidDetails, validDetails, validLayout, invalidLayout;
    CardView cardView;
    NeumorphFloatingActionButton incrementBtn, decreaseBtn;
    NeumorphCardView enterButton;
    TextView numOfVisitors, backButton, buttonText, cardHolderName, cardType, cardNumber, logout, validityInfo, validityDetails;

    Boolean isInvalid;
    //TODO: Make change here and remove boolean value initialization
    Boolean isExpired;
    String name, card_num;
    Integer visitors, card_type, card_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_verificaiton);
        findViewsById();

        name = getIntent().getStringExtra(AdDisplayActivity.Name);
        card_num = getIntent().getStringExtra(AdDisplayActivity.UserCardNumber);
        card_type = getIntent().getIntExtra(AdDisplayActivity.UserCardType, 0);
        card_status = getIntent().getIntExtra(AdDisplayActivity.UserCardStatus, 0);
        isInvalid = getIntent().getBooleanExtra(AdDisplayActivity.IsInvalid, false);
        isExpired = getIntent().getBooleanExtra(AdDisplayActivity.IsExpired, false);

        cardHolderName.setText(name);
        cardNumber.setText(card_num);

        setCardDesign(card_type);
        isCardExpiredOrInvalid();

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

        validDetails = findViewById(R.id.active_card_details);
        invalidDetails = findViewById(R.id.invalid_card_details);
        validLayout = findViewById(R.id.card_validity_layout);
        invalidLayout = findViewById(R.id.card_invalidity_layout);

        validityInfo = findViewById(R.id.validity_info);
        validityDetails = findViewById(R.id.validity_details);

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

    private void isCardExpiredOrInvalid() {
        if (isExpired) {
            invalidLayout.setVisibility(View.VISIBLE);
            validLayout.setVisibility(View.GONE);
            validDetails.setVisibility(View.GONE);
            invalidDetails.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            invalidLayout.setBackgroundResource(R.drawable.expired_card);
            validityInfo.setText(R.string.expired_card);
            validityDetails.setText(R.string.expired_card_details);

        } else if (isInvalid || card_type == 0 || card_status == 0 || card_status == 2) {
            invalidLayout.setVisibility(View.VISIBLE);
            validDetails.setVisibility(View.GONE);
            validLayout.setVisibility(View.GONE);
            invalidDetails.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            invalidLayout.setBackgroundResource(R.drawable.invalid_card);
            validityInfo.setText(R.string.invalid_card);
            validityDetails.setText(R.string.invalid_card_details);

        } else {
            validDetails.setVisibility(View.VISIBLE);
            validLayout.setVisibility(View.VISIBLE);
            invalidDetails.setVisibility(View.GONE);
            invalidLayout.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void setCardDesign(int card_type) {

        switch (card_type) {
            case 1:
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.dark_gray));
                cardType.setText(R.string.diamond);
                break;

            case 2:
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                cardType.setText(R.string.platinum);
                break;

            case 3:
                cardView.setBackgroundTintList(getResources().getColorStateList(R.color.gold));
                cardType.setTextColor(getResources().getColor(R.color.dark_gray));
                cardType.setText(R.string.gold);
                cardNumber.setTextColor(getResources().getColor(R.color.dark_gray));
                cardHolderName.setTextColor(getResources().getColor(R.color.dark_gray));
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

            if (isExpired || isInvalid) {
                onBackPressed();
            } else {
                incrementBtn.setVisibility(View.VISIBLE);
                decreaseBtn.setVisibility(View.VISIBLE);

                numOfVisitors.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                numOfVisitors.setTextColor(getResources().getColor(R.color.dark_gray));

                buttonText.setText("ENTER");
                backButton.setVisibility(View.GONE);
            }

        }
    };

}