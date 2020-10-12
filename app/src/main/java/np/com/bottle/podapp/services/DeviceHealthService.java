package np.com.bottle.podapp.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.StrictMode;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class DeviceHealthService extends IntentService implements SensorEventListener {

    private long mStartRX;
    private long mStartTX;

    private String TAG = "DeviceHealthService";
    private AppPreferences appPref;
    private DatabaseHelper helper;
    private static int sLastCpuCoreCount = -1;
    private float batteryTemp;
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

        Intent intent = new Intent();
        batteryTemp = ((float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10);

        getDeviceHealthStatus();

        //Obtaining CPU Temperature, Codes from the library:
        Map<String, String> overriddenConfig = new HashMap<String, String>();
        overriddenConfig.put("debugMode", "true");

      /*  Components components = JSensors.get.config(overriddenConfig).components();
        List<Cpu> cpus = components.cpus;
        if (cpus != null) {
            for (final Cpu cpu : cpus) {
                System.out.println("Found CPU component: " + cpu.name);
                if (cpu.sensors != null) {
                    System.out.println("Sensors: ");

                    //Print temperatures
                    List<Temperature> temps = cpu.sensors.temperatures;
                    for (final Temperature temp : temps) {
                        System.out.println(temp.name + ": " + temp.value + " C");
                    }

                    //Print fan speed
                    List<Fan> fans = cpu.sensors.fans;
                    for (final Fan fan : fans) {
                        System.out.println(fan.name + ": " + fan.value + " RPM");
                    }
                }
            }
        }*/

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

        myCPUTempReader();

        /*float cpu_temp = getCurrentCPUTemperature();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            cpu_temp = cpu_temp / 1000;
            Log.i(TAG, "Cpu Temp: " + cpu_temp + "C");
        }*/

        mStartRX = TrafficStats.getTotalRxBytes();
        mStartTX = TrafficStats.getTotalTxBytes();

        //TODO: call wifiSpeed method in the same timer-task used while sending value to API and storing to db, try calling methods on same timertask
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
        } catch (
                Exception e) {
            e.printStackTrace();
            return 0.0f;
        }

    }

    private float getCurrentCPUTemperature() {
        String file = readFile("/sys/devices/virtual/thermal/thermal_zone0/temp", '\n');

        if (file != null) {
            return Long.parseLong(file);
        } else {
            return Long.parseLong(batteryTemp + " " + (char) 0x00B0 + "C");
        }
    }

    private byte[] mBuffer = new byte[4096];

    @SuppressLint("NewApi")
    private String readFile(String file, char endChar) {
        // Permit disk reads here, as /proc/meminfo isn't really "on
        // disk" and should be fast.  TODO: make BlockGuard ignore
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
        }
        return null;
    }

    private void myCPUTempReader() {

        ArrayList<String> temp_dir_list = new ArrayList<>();
        temp_dir_list.add("/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp");
        temp_dir_list.add("/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp");
        temp_dir_list.add("/sys/class/thermal/thermal_zone1/temp");
        temp_dir_list.add("/sys/class/i2c-adapter/i2c-4/4-004c/temperature");
        temp_dir_list.add("/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature");
        temp_dir_list.add("/sys/devices/platform/omap/omap_temp_sensor.0/temperature");
        temp_dir_list.add("/sys/devices/platform/tegra_tmon/temp1_input");
        temp_dir_list.add("/sys/kernel/debug/tegra_thermal/temp_tj");
        temp_dir_list.add("/sys/devices/platform/s5p-tmu/temperature");
        temp_dir_list.add("/sys/class/thermal/thermal_zone0/temp");
        temp_dir_list.add("/sys/devices/virtual/thermal/thermal_zone0/temp");
        temp_dir_list.add("/sys/class/hwmon/hwmon0/device/temp1_input");
        temp_dir_list.add("/sys/devices/virtual/thermal/thermal_zone1/temp");
        temp_dir_list.add("/sys/devices/platform/s5p-tmu/curr_temp");

        File cputempfile = null;

        for (String dir : temp_dir_list) {
            cputempfile = new File(dir);

            if (cputempfile.exists()) {
                String[] array = new String[temp_dir_list.size()]; // Converting Array of String to String Array
                for (int j = 0; j < temp_dir_list.size(); j++) {
                    array[j] = temp_dir_list.get(j);
                    Log.i(TAG, "Cpu Temp: " + ReadCPU0(array));
                }
            } else {
                Log.i(TAG, "Cpu Temp: " + "Sorry doesn't exist");
            }
        }

        /*String[] cputemp = {"/system/bin/cat", "sys/class/thermal/thermal_zone0/temp"};
        File cputempfile = new File("sys/class/thermal/thermal_zone0/temp");
        if (cputempfile.exists()) {
            ReadCPU0(cputemp);
            Log.i(TAG, "Cpu Temp: " + ReadCPU0(cputemp));
        } else {
            Log.i(TAG, "Cpu Temp: " + "Sorry doesn't exist");
        }*/

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert mSensorManager != null;
        mSensorManager.registerListener(temperatureSensor, mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);

    }

    private String ReadCPU0(String[] input) {
        ProcessBuilder pB;
        String result = "";

        try {
            //String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"};
            pB = new ProcessBuilder(input);
            pB.redirectErrorStream(false);
            Process process = pB.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) //default -1
            {
                //System.out.println(new String(re));
                result = new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private SensorEventListener temperatureSensor = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            System.out.println("on sensor changed called");
            float temp = event.values[0];
            System.out.println("Temperature sensor: " + temp);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

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
//            Log.i(TAG, "UploadSpeed: " + uplink + " DownloadSpeed: " + downlink);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambient_temperature = sensorEvent.values[0];
        Log.i(TAG, "Ambient Temperature:\n " + (ambient_temperature) + "C");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
