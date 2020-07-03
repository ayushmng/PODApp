package np.com.bottle.podapp.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

public class Helper {
    private static String TAG = Helper.class.getSimpleName();

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

    /**
     * Removes garbage characters from the string obtained by from NFC card.
     * @param data string NFC data
     * @return cleaned string data
     */
    public static String cleanNFCData(String data) {
        if (data != null) {
            return data.replaceAll("[^a-zA-Z0-9-_ ]","");
        } else {
            return "";
        }
    }

    /**
     * Conversion of Hexadecimal values to String.
     */
    public static String convertHexToString(byte[] byteData) {
        String strData = new String(byteData, StandardCharsets.UTF_8);
        return cleanNFCData(strData);
    }

    /**
     * Compares a date value with the current date.
     * @param strDate value of the date to compare
     * @return true if it is Today's date or false if not.
     */
    public static Boolean compareDate(String strDate) {
        if(strDate.equals("")) {
            return false;
        }

        Calendar currentCal = Calendar.getInstance();

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(strDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error in parsing date");
            return false;
        }
        Calendar givenCal = Calendar.getInstance();
        if (date != null) {
            givenCal.setTime(date);
        } else {
            return false;
        }

        if (currentCal.get(Calendar.DATE) == givenCal.get(Calendar.DATE)) {
            return true;
        } else {
            return false;
        }
    }

    public static void fileCount(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d(TAG, "Files Count: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "Filename: " + files[i].getName());
        }
    }
}
