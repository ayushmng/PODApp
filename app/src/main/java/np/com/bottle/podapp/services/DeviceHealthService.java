package np.com.bottle.podapp.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

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

    private String TAG = "DeviceHealthService";
    private AppPreferences appPref;
    private DatabaseHelper helper;
    ArrayList<DeviceHealth> deviceHealthList = new ArrayList<DeviceHealth>();

    // Device Health
    Timer healthTimer = new Timer();

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

        String deviceId, payloadType, fleetId, freeRam, cpuUsage, cpuTemperature, rssi, uplink, downlink, log, status;

        deviceId = appPref.getString(AppPreferences.DEVICE_ID);
        payloadType = Constants.PAYLOAD_TYPE_HEALTH;
        fleetId = appPref.getString(AppPreferences.FLEET_ID);
        freeRam = (new Random().nextInt(956) + 128) + "mb"; // Dummy value
        cpuUsage = (new Random().nextInt(85) + 15) + "%"; // Dummy value
        cpuTemperature = (new Random().nextInt(45)) + "c"; // Dummy value. Float.toString(Helper.getCurrentCPUTemperatureInCelcius())
        rssi = Integer.toString(wifiInfo.getRssi());
        uplink = (new Random().nextInt(5) + 1) + "mbps"; // Dummy value
        downlink = (new Random().nextInt(10) + 1) + "mbps"; // Dummy value
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
