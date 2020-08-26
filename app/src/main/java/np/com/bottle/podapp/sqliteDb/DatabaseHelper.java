package np.com.bottle.podapp.sqliteDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import np.com.bottle.podapp.models.DeviceHealth;

public class DatabaseHelper extends SQLiteOpenHelper {

    final Context mContext;
    static String name = "DeviceHealth";
    static int version = 1;

    String createtable = "CREATE TABLE if not exists device_health (\n" +
            "\t\"id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t\"deviceId\"\tTEXT,\n" +
            "\t\"payloadType\"\tTEXT,\n" +
            "\t\"fleetId\"\tTEXT,\n" +
            "\t\"freeRam\"\tTEXT,\n" +
            "\t\"cpuUsage\"\tTEXT,\n" +
            "\t\"cpuTemperature\"\tTEXT,\n" +
            "\t\"rssi\"\tTEXT,\n" +
            "\t\"uplink\"\tTEXT,\n" +
            "\t\"downlink\"\tTEXT,\n" +
            "\t\"log\"\tTEXT,\n" +
            "\t\"status\"\tTEXT\n" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, name, null, version);
        this.mContext = context;
        getWritableDatabase().execSQL(createtable);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertData(ContentValues cv) {
/*//---------------------------------------- If same item already exits then below code works ----------------------------------------------------//

        Cursor cursor = getReadableDatabase().rawQuery("select * from user where Name=?", new String[]{cv.getAsString("Name")});

        if (cursor.getCount() != 0) {
            //updte quantity here
            getWritableDatabase().update("device_health", cv, "Name= ?", new String[]{cv.getAsString("Name")});
            Toast.makeText(mContext, "Same item already exists, Updating " + cv.getAsString("Name") + " quantity as " + cv.getAsString("Quantity"), Toast.LENGTH_LONG).show();

        } else {
            //insert new item here
            getWritableDatabase().insert("device_health", "", cv);
        }
// -------------------------------------- Ends over here ------------------------------------------------//*/
        getWritableDatabase().insert("device_health", "", cv);
    }

    public void updateData(ContentValues cv) {
        getWritableDatabase().update("device_health", cv, "id=1", null);
    }

    public int deleteData(String id) {
        return getWritableDatabase().delete("device_health", "id=" + id, null); // to delete single products
    }

    public void delete_table() {
        SQLiteDatabase db = this.getWritableDatabase(); // to delete whole table
        db.execSQL("delete from device_health");
    }

    public ArrayList<DeviceHealth> getDeviceHealthList() {

        ArrayList<DeviceHealth> list = new ArrayList<DeviceHealth>();

        String sql = "select * from device_health";

        Cursor c = getWritableDatabase().rawQuery(sql, null);

        Log.i("view_data", DatabaseUtils.dumpCursorToString(c));

        while (c.moveToNext()) {

            DeviceHealth info = new DeviceHealth();

            info.setDeviceId(c.getString(c.getColumnIndex("deviceId")));
            info.setPayloadType(c.getString(c.getColumnIndex("payloadType")));
            info.setFleetId(c.getString(c.getColumnIndex("fleetId")));
            info.setFreeRam(c.getString(c.getColumnIndex("freeRam")));
            info.setCpuUsage(c.getString(c.getColumnIndex("cpuUsage")));
            info.setCpuTemperature(c.getString(c.getColumnIndex("cpuTemperature")));
            info.setRssi(c.getString(c.getColumnIndex("rssi")));
            info.setUplink(c.getString(c.getColumnIndex("uplink")));
            info.setDownlink(c.getString(c.getColumnIndex("downlink")));
            info.setLog(c.getString(c.getColumnIndex("log")));
            info.setStatus(c.getString(c.getColumnIndex("status")));

            list.add(info);
        }
        c.close();

        return list;
    }
}
