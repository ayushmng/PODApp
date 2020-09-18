package np.com.bottle.podapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import np.com.bottle.podapp.R;
import np.com.bottle.podapp.models.Media;

public class MediaContentAdapter extends PagerAdapter {

    private static String TAG = MediaContentAdapter.class.getSimpleName();

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private LayoutInflater inflater;
    private Context context;
    private List<Media> mediaList;

    public MediaContentAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mediaList = mediaList;
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {

        View mediaLayout = inflater.inflate(R.layout.media_content_layout, view, false);

        Log.i(TAG, "currentPageFromAdapter: " + position);

        assert mediaLayout != null;
        ImageView imageView = mediaLayout.findViewById(R.id.ivImage);
        playerView = mediaLayout.findViewById(R.id.pvVideo);

        player = ExoPlayerFactory.newSimpleInstance(context);
        MediaSource mediaSource = buildMediaSource(mediaList.get(position).MediaUri);

        Log.i(TAG, "Media Type: " + mediaList.get(position).MediaType);

        if (mediaList.get(position).MediaType.equals(Media.MEDIA_TYPE_IMAGE)) {
//            imageView.setImageURI(IMAGES.get(position));
            imageView.setImageURI(mediaList.get(position).MediaUri);
            imageView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);

        } else {

            player.prepare(mediaSource);
            playerView.setPlayer(player);
            playerView.setVisibility(View.VISIBLE);
//            player.setPlayWhenReady(true);

            /*player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {

                        case Player.STATE_IDLE:
                        case Player.STATE_ENDED: {
                            setPlayWhenReady(false);
                            listener.onVideoEnds(true, true); //Notify AdDisplayActivity that video has end
                        }
                        break;
                        case Player.STATE_BUFFERING:
                        case Player.STATE_READY:
                            break;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    playerView.onPause();
                }
            });*/
        }

        mediaLayout.setTag(position);
        view.addView(mediaLayout, 0);
        return mediaLayout;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

}
