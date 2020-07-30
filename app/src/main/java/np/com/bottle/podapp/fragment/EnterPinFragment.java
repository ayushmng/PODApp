package np.com.bottle.podapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import np.com.bottle.podapp.R;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.ShapeType;


public class EnterPinFragment extends DialogFragment implements DigitAdapter.buttonClickListener {

    DigitAdapter adapter;
    RecyclerView digitRecyclerView;
    NeumorphCardView clear, zero, backspace;
    TextView textField, cancel;

    String pin;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EnterPinFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static EnterPinFragment newInstance(String param1, String param2) {
        EnterPinFragment fragment = new EnterPinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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

        digitRecyclerView = view.findViewById(R.id.digits_recyclerView);
        clear = view.findViewById(R.id.nu_clear);
        zero = view.findViewById(R.id.nu_zero);
        backspace = view.findViewById(R.id.nu_backspace);
        textField = view.findViewById(R.id.tv_digits);
        cancel = view.findViewById(R.id.tv_cancel);

        pin = textField.getText().toString();

        clear.setOnClickListener(clearClickListener);
        zero.setOnClickListener(zeroClickListener);
        backspace.setOnClickListener(backClickListener);
        cancel.setOnClickListener(cancelClickListener);

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