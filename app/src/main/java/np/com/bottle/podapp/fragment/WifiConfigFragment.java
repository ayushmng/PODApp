package np.com.bottle.podapp.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.List;

import np.com.bottle.podapp.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class WifiConfigFragment extends DialogFragment {

    private static String TAG = WifiConfigFragment.class.getSimpleName();

    public static final String ARG_PARAM1 = "scanresult";

    private Context context;
    private ScanResult scanResult;

    TextView tvSsid;
    EditText etPassword;
    Button btnConnection;

    public WifiConfigFragment(Context context) {
        // Required empty public constructor
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            String strScanResult = getArguments().getString(ARG_PARAM1);
//            Log.d(TAG, "strScanResult ------- " + strScanResult);
            scanResult = getArguments().getParcelable(ARG_PARAM1);
            Log.d(TAG, "ssid ------- " + scanResult.SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_wifi_config, container, false);

        View view = inflater.inflate(R.layout.fragment_wifi_config, container, false);

        tvSsid = view.findViewById(R.id.tvSsid);
        etPassword = view.findViewById(R.id.etPassword);
        btnConnection = view.findViewById(R.id.btnConnect);

        tvSsid.setText(scanResult.SSID);

        buttonClickable(false);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                buttonClickable(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty() || charSequence.toString().length() < 8) {
                    buttonClickable(false);
                } else {
                    buttonClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnConnection.setOnClickListener(btnConnectionListener);

        return view;
    }

    private void buttonClickable(Boolean status) {
        if (!status) {
            btnConnection.setTextColor(getResources().getColor(R.color.gray));
            btnConnection.setClickable(false);
        } else {
            btnConnection.setClickable(true);
            btnConnection.setTextColor(getResources().getColor(R.color.dark_gray));
        }
    }

    private View.OnClickListener btnConnectionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String password = etPassword.getText().toString();
            connectWifi(password);
        }
    };

    public void connectWifi(String password) {
        if (!password.equals("")) {
            if (Build.VERSION.SDK_INT < 29) {
                Log.d(TAG, "here <29");

                WifiConfiguration wifiConf = new WifiConfiguration();
//                    wifiConf.SSID = "\"" + scanResult.SSID + "\"";
//                    wifiConf.preSharedKey = "\"" + password + "\"";

                wifiConf.SSID = String.format("\"%s\"", scanResult.SSID);
                wifiConf.preSharedKey = String.format("\"%s\"", password);

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                assert wifiManager != null;
                wifiManager.addNetwork(wifiConf);

                List<WifiConfiguration> wifiConfigList = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration result : wifiConfigList) {
                    if (result.SSID != null && result.SSID.equals("\"" + scanResult.SSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(result.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
            } else {
                Log.d(TAG, "here 29");
                WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                builder.setSsid(scanResult.SSID);
                builder.setWpa2Passphrase(password);

                WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
                networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

                NetworkRequest networkRequest = networkRequestBuilder.build();

                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        Log.d(TAG, "onAvailable:" + network);
                        assert connectivityManager != null;
                        connectivityManager.bindProcessToNetwork(network);
                        dismiss();

                    }
                };
                assert connectivityManager != null;
                connectivityManager.requestNetwork(networkRequest, networkCallback);
            }
        }
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }
}