package np.com.bottle.podapp;

import android.content.Context;
import android.content.SharedPreferences;

public class ContentPreferences {
    private SharedPreferences preferences;
    private final String APP_PREFERENCES_FILE_NAME = "content_details";

    public final static String CONTENT_DATA = "content_data";
    public final static String CONTENT_DATE = "content_date";
    public final static String PRIORITY = "priority";

    public ContentPreferences(Context context) {
        preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        String v = value;
        if(v == null) {
            v = "";
        }
        editor.putString(key, v);
        editor.apply();
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
