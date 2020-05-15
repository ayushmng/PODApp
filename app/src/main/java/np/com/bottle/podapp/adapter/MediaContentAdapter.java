package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import np.com.bottle.podapp.R;

public class MediaContentAdapter extends PagerAdapter {

    private static String TAG = MediaContentAdapter.class.getSimpleName();

    private ArrayList<Uri> IMAGES;
    private LayoutInflater inflater;
    private Context context;

    public MediaContentAdapter(Context context, ArrayList<Uri> IMAGES) {
        this.context = context;
        this.IMAGES = IMAGES;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.media_content_layout, view, false);

        assert imageLayout != null;
        ImageView imageView = imageLayout.findViewById(R.id.ivImage);

        imageView.setImageURI(IMAGES.get(position));
        imageView.setVisibility(View.VISIBLE);

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
