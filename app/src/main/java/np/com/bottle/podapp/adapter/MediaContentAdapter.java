package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.models.Media;

public class MediaContentAdapter extends PagerAdapter {

    private static String TAG = MediaContentAdapter.class.getSimpleName();

    private ArrayList<Uri> IMAGES;
    private LayoutInflater inflater;
    private Context context;
    private List<Media> mediaList;

    public MediaContentAdapter(Context context, ArrayList<Uri> IMAGES, List<Media> mediaList) {
        this.context = context;
        this.IMAGES = IMAGES;
        inflater = LayoutInflater.from(context);
        this.mediaList = mediaList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {

        View mediaLayout = inflater.inflate(R.layout.media_content_layout, view, false);

        assert mediaLayout != null;
        ImageView imageView = mediaLayout.findViewById(R.id.ivImage);
        PlayerView playerView = mediaLayout.findViewById(R.id.pvVideo);

        if(mediaList.get(position).MediaType.equals(Media.MEDIA_TYPE_IMAGE)) {
//            imageView.setImageURI(IMAGES.get(position));
            imageView.setImageURI(mediaList.get(position).MediaUri);
            imageView.setVisibility(View.VISIBLE);
        } else {
            SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context);
            MediaSource mediaSource = buildMediaSource(mediaList.get(position).MediaUri);

            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            playerView.setPlayer(player);
            playerView.setVisibility(View.VISIBLE);
        }

//        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context);
//        MediaSource mediaSource = buildMediaSource(mediaList.get(1).MediaUri);
//
//        Log.d(TAG, "media uri: " + mediaList.get(1).MediaUri);
//
//        player.prepare(mediaSource);
//        player.setPlayWhenReady(true);
//        playerView.setVisibility(View.VISIBLE);
//
//        playerView.setPlayer(player);

        view.addView(mediaLayout, 0);

        return mediaLayout;
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

    @Override
    public int getItemPosition(Object object) {
        return 0;
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "exoplayer-codelab");

        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }
}
