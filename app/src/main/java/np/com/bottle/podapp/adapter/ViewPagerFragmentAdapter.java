package np.com.bottle.podapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import np.com.bottle.podapp.fragment.DeviceDetailsSettingsFragment;
import np.com.bottle.podapp.models.DataList;
import np.com.bottle.podapp.util.Constants;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    private List<DataList> deviceDetailList;

    public ViewPagerFragmentAdapter(FragmentActivity fragmentActivity, List<DataList> list) {
        super(fragmentActivity);
        this.deviceDetailList = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new DeviceDetailsSettingsFragment(deviceDetailList, position);
    }

    @Override
    public int getItemCount() {
        return Math.round(deviceDetailList.size() / Constants.ITEMS_PER_PAGE);
    }
}
