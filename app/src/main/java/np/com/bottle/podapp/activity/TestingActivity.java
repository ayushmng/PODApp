package np.com.bottle.podapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import np.com.bottle.podapp.R;

public class TestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Page1");
        arrayList.add("Page2");

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new CustomPagerAdapter(this, arrayList));
    }
}

class CustomPagerAdapter extends PagerAdapter {

    private String TAG = "TestingActivity";
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<String> list;

    public CustomPagerAdapter(Context context, ArrayList<String> arrayList) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.list = arrayList;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup view, int position) {

        View mediaLayout = inflater.inflate(R.layout.test_adapter_layout, view, false);
        TextView textView = mediaLayout.findViewById(R.id.textView);
        textView.setText(list.get(position));
        Log.i(TAG, "Called here: " + position);

        return mediaLayout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(Integer.parseInt(list.get(position)));
    }

}