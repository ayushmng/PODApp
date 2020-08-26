package np.com.bottle.podapp.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

public class Helper {
    private static String TAG = Helper.class.getSimpleName();

    private static byte[] mBuffer = new byte[4096];

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

    public static float getCurrentCPUTemperatureInCelcius() {
        String file = readFile("/sys/devices/virtual/thermal/thermal_zone0/temp", '\n');
        if (file != null) {
            return (float) (Long.parseLong(file) / 1000);
        } else {
            return 0;

        }
    }

    private static String readFile(String file, char endChar) {
        // Permit disk reads here, as /proc/meminfo isn't really "on
        // disk" and should be fast.
        // /proc/ and /sys/ files perhaps?
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            int len = is.read(mBuffer);
            is.close();

            if (len > 0) {
                int i;
                for (i = 0; i < len; i++) {
                    if (mBuffer[i] == endChar) {
                        break;
                    }
                }
                return new String(mBuffer, 0, i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
        }
        return null;
    }

    public static String generateMqttCommandPayload(
            String deviceId,
            String payloadType,
            String action,
            String module,
            String packageName) {

        JSONObject parentPayload = new JSONObject();
        JSONObject essentialPayload = new JSONObject();
        JSONObject payload = new JSONObject();

        try {
            essentialPayload.put("deviceID", deviceId);
            essentialPayload.put("payloadType", payloadType);

            payload.put("action", action);
            payload.put("module", module);
            payload.put("packageName", packageName);

            essentialPayload.put("payload", payload);

            parentPayload.put("essential", essentialPayload);

            return parentPayload.toString();
        } catch (Exception e) {
            Log.e(TAG, "Json parsing error");
            Log.e(TAG, "Error: " + e.getMessage());
            return "";
        }

    }

    public static String generateMqttDeviceHealthPayload(
            String deviceId,
            String payloadType,
            String fleetId,
            String freeRam,
            String cpuUsage,
            String cpuTemperature,
            String rssi,
            String uplink,
            String downlink,
            String log,
            String status) {

        JSONObject parentPayload = new JSONObject();
        JSONObject essentialPayload = new JSONObject();
        JSONObject payload = new JSONObject();

        try {
            essentialPayload.put("deviceID", deviceId);
            essentialPayload.put("payloadType", payloadType);

            payload.put("fleetID", fleetId);
            payload.put("freeRam", freeRam);
            payload.put("cpuUsage", cpuUsage);
            payload.put("cpuTemperature", cpuTemperature);
            payload.put("rssi", rssi);
            payload.put("uplink", uplink);
            payload.put("downlink", downlink);
            payload.put("log", log);
            payload.put("status", status);

            essentialPayload.put("payload", payload);

            parentPayload.put("essential", essentialPayload);

            return parentPayload.toString();
        } catch (Exception e) {
            Log.e(TAG, "Json parsing error");
            Log.e(TAG, "Error: " + e.getMessage());
            return "";
        }
    }

}
