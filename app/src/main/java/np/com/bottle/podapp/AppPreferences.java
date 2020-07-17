package np.com.bottle.podapp;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private SharedPreferences preferences;
    private final String APP_PREFERENCES_FILE_NAME = "aws_credential";

    public final static String CUSTOMER_SPECIFIC_ENDPOINT_ARN = "endpoint_arn";

    public final static String DEVICE_NAME = "name";
    public final static String DEVICE_ID = "deviceId";
    public final static String ACTIVATION_CODE = "activationCode";
    public final static String DEVICE_URI = "device_uri";
    public final static String FLEET_ID = "fleetId";
    public final static String MQTT_HOST = "host";
    public final static String GROUP_ID = "groupId";
    public final static String ORGANISATION_ID = "orgId";
    public final static String CLIENT_ID = "clientId";
    public final static String ANDROID_NAME = "androidName";

    public final static String AWS_IOT_CERTIFICATE = "Certificate";
    public final static String AWS_IOT_PRIVATE_KEY = "Private";
    public final static String AWS_IOT_PUBLIC_KEY = "Public";

    public final static String IS_PROVISIONED = "provision_status";
    public final static String IS_KIOSK_MODE = "kiosk_status";

    public AppPreferences(Context context) {
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
