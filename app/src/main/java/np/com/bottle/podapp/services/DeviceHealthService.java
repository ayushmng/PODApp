package np.com.bottle.podapp.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import np.com.bottle.podapp.AppPreferences;
import np.com.bottle.podapp.models.DeviceHealth;
import np.com.bottle.podapp.retrofit.ApiClient;
import np.com.bottle.podapp.retrofit.ApiService;
import np.com.bottle.podapp.sqliteDb.DatabaseHelper;
import np.com.bottle.podapp.util.Constants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DeviceHealthService extends IntentService {

    private long mStartRX;
    private long mStartTX;

    private String TAG = "DeviceHealthService";
    private AppPreferences appPref;
    private DatabaseHelper helper;
    private static int sLastCpuCoreCount = -1;
    private String deviceId, payloadType, fleetId, freeRam, cpuUsage, cpuTemperature, rssi, uplink, downlink, log, status;
    ArrayList<DeviceHealth> deviceHealthList = new ArrayList<DeviceHealth>();

    public DeviceHealthService() {
        super("DeviceHealthService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        appPref = new AppPreferences(getApplicationContext());
        helper = new DatabaseHelper(this);
        deviceHealthList = helper.getDeviceHealthList();
        deviceMetrics();
    }

    // Todo: Device metrics [CPU usage, temperature, RAM usage] need to be implemented.
    public void deviceMetrics() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        deviceId = appPref.getString(AppPreferences.DEVICE_ID);
        payloadType = Constants.PAYLOAD_TYPE_HEALTH;
        fleetId = appPref.getString(AppPreferences.FLEET_ID);

        getDeviceHealthStatus();

        //TODO: Check if both returns same value or not...i.e. the commented section provides temp. in Celcius
        cpuTemperature = String.valueOf(cpuTemperature()); //Float.toString(Helper.getCurrentCPUTemperatureInCelcius())
        rssi = Integer.toString(wifiInfo.getRssi());
        log = "";
        status = "";

        ContentValues cv = new ContentValues();
        cv.put("deviceId", deviceId);
        cv.put("payloadType", payloadType);
        cv.put("fleetId", fleetId);
        cv.put("freeRam", freeRam);
        cv.put("cpuUsage", cpuUsage);
        cv.put("cpuTemperature", cpuTemperature);
        cv.put("rssi", rssi);
        cv.put("uplink", uplink);
        cv.put("downlink", downlink);
        cv.put("log", log);
        cv.put("status", status);

        if (deviceHealthList.isEmpty()) {
            helper.insertData(cv);
        } else {
            helper.updateData(cv);
        }

        if (isConnected(this)) {
//            postData(getData());
        } else {
            Toast.makeText(this, "No internet Connection, Please make sure you are connected with internet", Toast.LENGTH_SHORT).show();
        }

        //TODO: Open this commented out section, while sending data to server and store data to database
        /*TimerTask healthTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String payload = Helper.generateMqttDeviceHealthPayload(
                            deviceId,
                            payloadType,
                            fleetId,
                            freeRam,
                            cpuUsage,
                            cpuTemperature,
                            rssi,
                            uplink,
                            downlink,
                            log,
                            status
                    );
                    Log.d(TAG, "Health: " + payload);
                    Log.d(TAG, "temp: " + Helper.getCurrentCPUTemperatureInCelcius());

                } catch (Exception e) {
                    Log.e(TAG, "Health data error: " + e.getMessage());
                }
            }
        };

        healthTimer.schedule(healthTimerTask, 1000, 15000);*/
    }

    private void getDeviceHealthStatus() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        freeRam = availableMegs + " MB";

        try {
            totalCpuUsage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mStartRX = TrafficStats.getTotalRxBytes();
        mStartTX = TrafficStats.getTotalTxBytes();

        //TODO: call wifiSpeed method in the same timertask used while sending value to API and storing to db, try calling methods on same timertask
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                wifiSpeed();
            }
        };
        timer.schedule(timerTask, 1000, 1000);

        //TODO: The below log contains String value "cpuUsage" which takes maxCpuUsage in diff. way if the method "maxCpuUsage" fails check with that
        // and its the only max CPU usage not the desired CPU usage

        /*//TODO: Check this works for new device or not
        assert obtainedCPU != null;
        float cpu_use = (Float.parseFloat(obtainedCPU) / Float.parseFloat(maxCpuUsage())) * 100;
        cpuUsage = cpu_use + "%";
        Log.i(TAG, "CPU Usage: " + obtainedCPU + " Max Cpu Usage: " + maxCpuUsage() + " CPU: " + cpuUsage);
        Log.i(TAG, "Free Ram: " + freeRam);*/
    }

    private void totalCpuUsage() throws FileNotFoundException {
        float sum = 0;
        String obtainedCPU = null;
        List<Float> cpuCoreList = new ArrayList<Float>();

        for (int i = 0; i < calcCpuCoreCount(); i++) {
            obtainedCPU = (takeCurrentCpuFreq(i) + "");

            float cpu_use = (Float.parseFloat(obtainedCPU) / Float.parseFloat(maxCpuUsage(i))) * 100;
            cpuCoreList.add(cpu_use);

            sum += cpuCoreList.get(i);
            Log.i(TAG, "CPU Usage: " + obtainedCPU + " Max Cpu Usage: " + maxCpuUsage(i));
        }

        float total_usage = sum / (cpuCoreList.size() * 100) * 100;
        cpuUsage = String.valueOf(total_usage).substring(0, 5);
        Log.i(TAG, "Total Cpu Usage: " + cpuUsage + "%");
    }

    //TODO: Check whether this directory works for the real device or not, and also check for temperature directory
    private String maxCpuUsage(int core) throws FileNotFoundException {
        String cpuMaxFreq = "";
        RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu" + core + "/cpufreq/cpuinfo_max_freq", "r"); // r -> read
        try {
            cpuMaxFreq = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuMaxFreq;
    }

    //TODO: Check whether this method returns value for real device or not
    public static float cpuTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            } else {
                return 51.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    private void wifiSpeed() {
        if (mStartRX != TrafficStats.UNSUPPORTED || mStartTX != TrafficStats.UNSUPPORTED) {

            long rxBytes = (TrafficStats.getTotalRxBytes() - mStartRX) / 1024;
            downlink = Long.toString(rxBytes);

            if (downlink.length() >= 4) {
                float mbps = (float) rxBytes / 1024;
                downlink = (String.valueOf(mbps)).substring(0, 4) + "mbps";
            } else {
                downlink = downlink + "kbps";
            }

            long txBytes = (TrafficStats.getTotalTxBytes() - mStartTX) / 1024;
            uplink = Long.toString(txBytes);

            if (uplink.length() >= 4) {
                float mbps = (float) txBytes / 1024;
                uplink = (String.valueOf(mbps)).substring(0, 4) + "mbps";
            } else {
                uplink = uplink + "kbps";
            }

            mStartRX = TrafficStats.getTotalRxBytes();
            mStartTX = TrafficStats.getTotalTxBytes();

            Log.i(TAG, "UploadSpeed: " + uplink + " DownloadSpeed: " + downlink);
        }
    }

    /*public static int[] getCPUFrequencyCurrent() throws Exception {
        int[] output = new int[getNumCores()];
        for (int i = 0; i < getNumCores(); i++) {
            output[i] = readSystemFileAsInt("/sys/devices/system/cpu/cpu" + String.valueOf(i) + "/cpufreq/scaling_cur_freq");
        }
        return output;
    }*/

    /*    private static int readSystemFileAsInt(final String pSystemFile) throws Exception {
        InputStream in = null;
        try {
            final Process process = new ProcessBuilder(new String[] { "/system/bin/cat", pSystemFile }).start();

            in = process.getInputStream();
            final String content = readFully(in);
            return Integer.parseInt(content);
        } catch (final Exception e) {
            throw new Exception(e);
        }
    }

    public static String readFully(final InputStream pInputStream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final Scanner sc = new Scanner(pInputStream);
        while(sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        return sb.toString();
    }*/

    private static int readIntegerFile(String filePath) {
        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)), 1000);
            final String line = reader.readLine();
            reader.close();

            return Integer.parseInt(line);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int takeCurrentCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/scaling_cur_freq");
    }

    public static int calcCpuCoreCount() {

        if (sLastCpuCoreCount >= 1) {
            return sLastCpuCoreCount;
        }

        try {
            // Get directory containing CPU info
            final File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            final File[] files = dir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    //Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });

            // Return the number of cores (virtual CPU devices)
            assert files != null;
            sLastCpuCoreCount = files.length;

        } catch (Exception e) {
            sLastCpuCoreCount = Runtime.getRuntime().availableProcessors();
        }

        return sLastCpuCoreCount;
    }

    private JsonArray getData() {

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.getReadableDatabase();
        deviceHealthList = databaseHelper.getDeviceHealthList();

        JsonArray array = new JsonArray();

        for (DeviceHealth deviceHealth : deviceHealthList) {

            JsonObject params = new JsonObject();

            params.addProperty("deviceId", deviceHealth.getDeviceId());
            params.addProperty("payloadType", deviceHealth.getPayloadType());
            params.addProperty("fleetId", deviceHealth.getFleetId());
            params.addProperty("freeRam", deviceHealth.getFreeRam());
            params.addProperty("cpuUsage", deviceHealth.getCpuUsage());
            params.addProperty("cpuTemperature", deviceHealth.getCpuTemperature());
            params.addProperty("rssi", deviceHealth.getRssi());
            params.addProperty("uplink", deviceHealth.getUplink());
            params.addProperty("downlink", deviceHealth.getDownlink());
            params.addProperty("log", deviceHealth.getLog());
            params.addProperty("status", deviceHealth.getStatus());

            array.add(params);
        }

        return array;
    }

    //Posting data to Server
    public void postData(final JsonArray jsonArray) {

        String url = "addSubUrlHere";

        ApiService apiService = ApiClient.getService().create(ApiService.class);
        Call<ResponseBody> apiCall = apiService.postData(url, jsonArray.toString());
        apiCall.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.i("Response", jsonArray.toString());

                if (response.isSuccessful()) {
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Server Error, Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Checks internet connection
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return true;
        } else
            return false;
    }
}
