package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//        tvLable.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        tvLable.setText(capitalize(aConfig.key));
        tvValue.setText(aConfig.value);
    }

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z-éá])([a-z-éá]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
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
