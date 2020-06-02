package np.com.bottle.podapp.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ContentDownloadIntentService extends IntentService {

    private static String TAG = ContentDownloadIntentService.class.getSimpleName();

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String CONTENT_DATA = "content_data";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result"; // 200 for success, 101 for fail;
    public static final String NOTIFICATION = "np.com.bottle.podapp";

    public ContentDownloadIntentService() {
        super("ContentDownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String contentData = intent.getStringExtra(CONTENT_DATA);
            String url = intent.getStringExtra(URL);
            String fileName = intent.getStringExtra(FILENAME);

            JSONObject jPayload = new JSONObject(contentData);
            JSONArray jaData = jPayload.getJSONArray("data");

            Log.d(TAG, "data: " + jPayload.getString("data"));
            Log.d(TAG, "array: " + jaData.getJSONObject(0));
            Log.d(TAG, "data array length: " + jaData.length());

            for (int i = 0; i < jaData.length(); i++) {
                JSONArray jaContents = jaData.getJSONObject(i).getJSONArray("contents");

                for (int j = 0; j < jaContents.length(); j++) {
                    JSONObject jContent = jaContents.getJSONObject(j);
                    Log.d(TAG, "Level: " + i + " ---- " + "Name: " + jContent.getString("name"));
                    downloadFile(jContent.getString("signed_url"), jContent.getString("name") + "." + jContent.getString("extension"));
                }
            }

            publishResults(200);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
            publishResults(101);
        }
    }

    private void downloadFile(String url, String fileName) throws IOException {
        java.net.URL u = new URL(url);
        URLConnection conn = u.openConnection();
        int contentLength = conn.getContentLength();

        DataInputStream stream = new DataInputStream(u.openStream());

        byte[] buffer = new byte[contentLength];
        stream.readFully(buffer);
        stream.close();

        File dataFile = new File(getFilesDir().getAbsolutePath() + "/content/" + fileName);

        DataOutputStream fos = new DataOutputStream(new FileOutputStream(dataFile));
        fos.write(buffer);
        fos.flush();
        fos.close();
    }

    private void publishResults(int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
}
