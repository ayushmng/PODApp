package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.models.DataList;

public class AwsConfigListAdapter extends RecyclerView.Adapter<AwsConfigListAdapter.ViewHolder> {

    private List<DataList> configList;

    public AwsConfigListAdapter(List<DataList> config) {
        configList = config;
    }

    @NonNull
    @Override
    public AwsConfigListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View configListView = inflater.inflate(R.layout.layout_listview, parent, false);

        ViewHolder viewHolder = new ViewHolder(configListView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataList aConfig = configList.get(position);

        TextView tvLable = holder.tvLable;
        TextView tvValue = holder.tvValue;

        tvLable.setText(aConfig.key);
        tvValue.setText(aConfig.value);
        Log.d("test", "asdfsadfsdfsdfasdf------------------------");
    }

    @Override
    public int getItemCount() {
        return configList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvLable;
        TextView tvValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLable = itemView.findViewById(R.id.tvLable);
            tvValue = itemView.findViewById(R.id.tvValue);
        }
    }
}
