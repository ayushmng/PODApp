package np.com.bottle.podapp.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.nio.charset.StandardCharsets;

import static android.content.Context.WIFI_SERVICE;

public class Helper {
    private static String TAG = Helper.class.getSimpleName();


    public static final int RESULT_REQUEST_NFC = 2;  // To ask nfc permission in android
    public static final int RESULT_REQUEST_EXTERNAL_STORAGE = 3;  // To ask external storage permission in android

    public static final String ALIAS_KEY_AES128 = "key_aes_128";


    /**
     * This method returns current IP address of the device in xxx.xxx.xxx.xxx format.
     * @param context -> application context
     * @return -> IP in string format
     */
    public static String getIpAddress(Context context) {
        WifiManager wifiManager;
        String strIpAddress = "";
        int ipAddress = 0;

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        try {
            assert wifiManager != null;
            ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }

        strIpAddress = (ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff);
        Log.d(TAG, "ipAddress: " + strIpAddress);
        return strIpAddress;
    }

    public static String cleanNFCData(String data) {
        if (data != null) {
            return data.replaceAll("[^a-zA-Z0-9-_ ]","");
        } else {
            return "";
        }
    }

    public static String convertHexToString(byte[] byteData) {
        String strData = new String(byteData, StandardCharsets.UTF_8);
        return cleanNFCData(strData);
    }
}
