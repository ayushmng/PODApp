package np.com.bottle.podapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.interfaces.OnItemClickListener;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    Context context;
    OnItemClickListener clickListener;
    List<ScanResult> scanResults;

    public WifiListAdapter(Context context, OnItemClickListener clickListener, List<ScanResult> scanResults) {
        this.context = context;
        this.clickListener = clickListener;
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View configListView = inflater.inflate(R.layout.layout_wifi_listview, parent, false);

        return new ViewHolder(configListView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ScanResult scanResult = scanResults.get(position);

        String ssid = "";
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = String.valueOf(wifiInfo.getSSID());
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        /*if (scanResult.SSID.contains(ssid)) {
            position = 0;
        }
        if (position == 0) {
            holder.tvConnect.setText("Connected");
            holder.tvConnect.setTypeface(Typeface.DEFAULT_BOLD);
            holder.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.tvConnect.setText("Connect");
            holder.tvConnect.setTypeface(Typeface.DEFAULT);
            holder.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.blue));
        }*/

        Log.i("ShowSSID", ssid);
        if (scanResults.get(position).toString().contains(ssid)) {
            Log.i("ShowSSID", ssid);
            holder.tvConnect.setText("Connected");
            holder.tvConnect.setTypeface(Typeface.DEFAULT_BOLD);
            holder.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.tvConnect.setText("Connect");
            holder.tvConnect.setTypeface(Typeface.DEFAULT);
            holder.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.blue));
        }

        int level = WifiManager.calculateSignalLevel(scanResults.get(position).level, 5);
        holder.tvSsid.setText(scanResult.SSID);

        if (level == 0 || level == 1) {
            holder.wifiStrength.setBackgroundResource(R.drawable.ic_low_strength);
        }

        if (level == 2 || level == 3) {
            holder.wifiStrength.setBackgroundResource(R.drawable.ic_average_strength);
        }

        if (level == 4 || level == 5) {
            holder.wifiStrength.setBackgroundResource(R.drawable.ic_full_strength);
        }

        String finalSsid = ssid;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Disallowing to the connected wifi
                if (!scanResults.get(position).toString().contains(finalSsid)) {
                    clickListener.onItemClicked(scanResult);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        ImageView wifiStrength;
        TextView tvSsid, tvConnect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraint_layout);
            wifiStrength = itemView.findViewById(R.id.wifi_strength);
            tvSsid = itemView.findViewById(R.id.tvSsid);
            tvConnect = itemView.findViewById(R.id.tv_connect);
        }
    }
}
