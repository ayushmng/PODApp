package np.com.bottle.podapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.adapter.AwsConfigListAdapter;
import np.com.bottle.podapp.models.DataList;
import np.com.bottle.podapp.util.Constants;

public class DeviceDetailsSettingsFragment extends Fragment {

    private List<DataList> list2;
    private List<DataList> deviceDetailList;
    private RecyclerView rvDeviceDetails;
    private AwsConfigListAdapter deviceDetailsAdapter;
    private int pagePosition;

    public DeviceDetailsSettingsFragment(List<DataList> deviceDetailList, int page) {
        this.deviceDetailList = deviceDetailList;
        this.pagePosition = page;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.device_details_list_view, container, false);

        rvDeviceDetails = root.findViewById(R.id.rvDeviceDetails);

        int pos = pagePosition * Constants.ITEMS_PER_PAGE;

        int lastPosition;
        lastPosition = pos + Constants.ITEMS_PER_PAGE;
        list2 = deviceDetailList.subList(pos, lastPosition);

        deviceDetailsAdapter = new AwsConfigListAdapter(list2);
        rvDeviceDetails.setAdapter(deviceDetailsAdapter);
        rvDeviceDetails.setLayoutManager(new LinearLayoutManager(getContext()));

        return root;
    }
}