package np.com.bottle.podapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import np.com.bottle.podapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NfcDetectFragment extends DialogFragment {

    private static String TAG = NfcDetectFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "name";
    private static final String ARG_PARAM2 = "cardNumber";

    private String name;
    private int cardNumber;

    TextView tvName;
    TextView tvCardNumber;
    Button btnOk;

    public NfcDetectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_PARAM1);
            cardNumber = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc_detect, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvCardNumber = view.findViewById(R.id.tvCardNumber);
        btnOk = view.findViewById(R.id.btnOk);

        tvName.setText(name);
        tvCardNumber.setText(String.valueOf(cardNumber));

        btnOk.setOnClickListener(btnOkListener);

        return view;
    }

    private View.OnClickListener btnOkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

}
