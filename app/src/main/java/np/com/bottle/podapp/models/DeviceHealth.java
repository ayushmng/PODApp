package np.com.bottle.podapp.models;

public class DeviceHealth {

    private String deviceId,
            payloadType,
            fleetId,
            freeRam,
            cpuUsage,
            cpuTemperature,
            rssi,
            uplink,
            downlink,
            log,
            status;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public String getFleetId() {
        return fleetId;
    }

    public void setFleetId(String fleetId) {
        this.fleetId = fleetId;
    }

    public String getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(String freeRam) {
        this.freeRam = freeRam;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getCpuTemperature() {
        return cpuTemperature;
    }

    public void setCpuTemperature(String cpuTemperature) {
        this.cpuTemperature = cpuTemperature;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getUplink() {
        return uplink;
    }

    public void setUplink(String uplink) {
        this.uplink = uplink;
    }

    public String getDownlink() {
        return downlink;
    }

    public void setDownlink(String downlink) {
        this.downlink = downlink;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeviceHealth{" +
                "deviceId='" + deviceId + '\'' +
                ", payloadType='" + payloadType + '\'' +
                ", fleetId='" + fleetId + '\'' +
                ", freeRam='" + freeRam + '\'' +
                ", cpuUsage='" + cpuUsage + '\'' +
                ", cpuTemperature='" + cpuTemperature + '\'' +
                ", rssi='" + rssi + '\'' +
                ", uplink='" + uplink + '\'' +
                ", downlink='" + downlink + '\'' +
                ", log='" + log + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
