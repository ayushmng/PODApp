package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.interfaces.OnItemClickListener;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    OnItemClickListener clickListener;
    List<ScanResult> scanResults;

    public WifiListAdapter (OnItemClickListener clickListener, List<ScanResult> scanResults) {
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ScanResult scanResult = scanResults.get(position);

        holder.tvSsid.setText(scanResult.SSID);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClicked(scanResult);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSsid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSsid = itemView.findViewById(R.id.tvSsid);
        }
    }
}
