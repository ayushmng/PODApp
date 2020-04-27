package np.com.bottle.podapp;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private SharedPreferences preferences;
    private final String APP_PREFERENCES_FILE_NAME = "aws_credential";

    public static String CUSTOMER_SPECIFIC_ENDPOINT_ARN = "endpoint_arn";
    public static String CLIENT_ID = "client_id";

    public static String DEVICE_NAME = "device_name";
    public static String DEVICE_ID = "device_id";
    public static String ACTIVATION_TOKEN = "activation_token";
    public static String DEVICE_URI = "device_uri";
    public static String FLEET_ID = "fleet_id";
    public static String MQTT_HOST = "mqtt_host";

    public static String AWS_IOT_CERTIFICATE = "iot_certificate";
    public static String AWS_IOT_PRIVATE_KEY = "iot_private_key";
    public static String AWS_IOT_PUBLIC_KEY = "iot_public_key";
    public static String IS_PROVISIONED = "provision_status";

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
