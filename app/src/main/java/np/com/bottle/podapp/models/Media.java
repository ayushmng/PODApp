package np.com.bottle.podapp.models;

import android.net.Uri;

public class Media {
    public Uri MediaUri;
    public int Interval ;
    public String MediaType;
    public int Priority;

    public Media(Uri uri, int interval, String mediaType, int priority) {
        this.MediaUri = uri;
        this.Interval = interval;
        this.MediaType = mediaType;
        this.Priority = priority;
    }

    public static final String MEDIA_TYPE_IMAGE = "photo";
    public static final String MEDIA_TYPE_VIDEO = "video";
}
