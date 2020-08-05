package np.com.bottle.podapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.activity.SettingsActivity;
import soup.neumorphism.NeumorphCardView;

public class EnterPinFragment extends DialogFragment implements DigitAdapter.buttonClickListener {

    ConstraintLayout constraintLayout_primary, constraintLayout_secondary;
    DilatingDotsProgressBar mDilatingDotsProgressBar;
    DigitAdapter adapter;
    RecyclerView digitRecyclerView;
    NeumorphCardView clear, zero, backspace;
    TextView textField, textHead, ok, cancel;

    String pin;
    Integer value;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public EnterPinFragment(Integer value) {
        this.value = value;
    }

    /*public static EnterPinFragment newInstance(String param1, String param2) {
        EnterPinFragment fragment = new EnterPinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final ConstraintLayout root = new ConstraintLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Creating the fullscreen dialog
        final Dialog dialog = new Dialog(Objects.requireNonNull(getActivity()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_pin, container, false);

        constraintLayout_primary = view.findViewById(R.id.constraint_layout1);
        constraintLayout_secondary = view.findViewById(R.id.constraint_layout2);
        digitRecyclerView = view.findViewById(R.id.digits_recyclerView);
        clear = view.findViewById(R.id.nu_clear);
        zero = view.findViewById(R.id.nu_zero);
        backspace = view.findViewById(R.id.nu_backspace);
        textField = view.findViewById(R.id.tv_digits);
        textHead = view.findViewById(R.id.textView8);
        ok = view.findViewById(R.id.tv_ok);
        cancel = view.findViewById(R.id.tv_cancel);
        mDilatingDotsProgressBar = view.findViewById(R.id.progress);

        pin = textField.getText().toString();

        clear.setOnClickListener(clearClickListener);
        zero.setOnClickListener(zeroClickListener);
        backspace.setOnClickListener(backClickListener);
        cancel.setOnClickListener(cancelClickListener);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pin = textField.getText().toString();
                checkPin();
            }
        });

        if (value == 0) {
            textHead.setText(getString(R.string.enter_pin_to_access_pod_settings)); // Ad Activity
        } else {
            textHead.setText(getString(R.string.enter_your_pin_to_continue)); // Entrance Activity
        }

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            list.add(i);
        }

        adapter = new DigitAdapter(this, list);
        digitRecyclerView.setAdapter(adapter);
        digitRecyclerView.setLayoutManager((new GridLayoutManager(getContext(), 3)));
        adapter.notifyDataSetChanged();

        return view;
    }

    private void checkPin() {

        if (value == 1) {
            if (pin.equals("1234")) { // For Entrance Activity
                constraintLayout_primary.setVisibility(View.GONE);
                constraintLayout_secondary.setVisibility(View.VISIBLE);
                mDilatingDotsProgressBar.showNow();
            } else {
                constraintLayout_primary.setVisibility(View.VISIBLE);
                constraintLayout_secondary.setVisibility(View.GONE);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDilatingDotsProgressBar.hideNow();
                    constraintLayout_primary.setVisibility(View.VISIBLE);
                    constraintLayout_secondary.setVisibility(View.GONE);
                    Log.d("Handler", "Running Handler");
                }
            }, 5000);

        } else {
            if (pin.equals("123456")) { // For Ad Activity
                dismiss();
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        }

    }

    private View.OnClickListener clearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            textField.setText("");
        }
    };

    private View.OnClickListener zeroClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            pin = textField.getText().toString();
            textField.setText(pin + "0");
        }
    };

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            pin = textField.getText().toString();
            if (pin.length() != 0) {
                pin = textField.getText().toString();
                pin = pin.substring(0, pin.length() - 1);
                textField.setText(pin);
            }
        }
    };

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(int position) {
        pin = textField.getText().toString();
        textField.setText(pin + (position + 1));
    }

}


class DigitAdapter extends RecyclerView.Adapter<DigitAdapter.ViewHolder> {

    private buttonClickListener clickListener;
    private List<Integer> digitList;

    public DigitAdapter(buttonClickListener clickListener, List<Integer> list) {
        this.clickListener = clickListener;
        digitList = list;
    }

    public interface buttonClickListener {
        void onClick(int position);
    }

    @NonNull
    @Override
    public DigitAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View configListView = inflater.inflate(R.layout.layout_digits_list, parent, false);

        DigitAdapter.ViewHolder viewHolder = new ViewHolder(configListView);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DigitAdapter.ViewHolder holder, int position) {

        holder.digitTextView.setText(digitList.get(position).toString());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return digitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        NeumorphCardView cardView;
        TextView digitTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.nu_cardView);
            digitTextView = itemView.findViewById(R.id.tv_digit);
        }
    }
}