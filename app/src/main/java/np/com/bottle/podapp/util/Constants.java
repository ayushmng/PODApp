package np.com.bottle.podapp.util;

public class Constants {
    public static final String SUCCESS = "success";
    public static final String KEYSTORE_NAME = "bottle_pod_keystore";
    public static final String KEYSTORE_PASSWORD = "?_Mb5XLc)K+Zk@Fxb6}T}KmdkT5m&`_<";
    public static final String CERTIFICATE_ID = "default";

    public static final int PORT = 8000;

    public static final String STAGE_NAME = "test";
    public static final String STAGE = "stage/" + STAGE_NAME;

    public static String TOPIC_ACTIVATE_PUB;
    public static String TOPIC_TELEMETRY_PUB;
    public static String TOPIC_NFC_PAYMENT_PUB;
    public static String TOPIC_CONTENT_PUB;

//    public static String TOPIC_ACTIVATE_SUB;
    public static String TOPIC_TELEMETRY_SUB;
    public static String TOPIC_CONTENT_SUB;

    public static final int PERMISSIONS_REQUEST_CODE = 1240;

    public static void constructTopic(String orgId, String deviceId) {
        TOPIC_ACTIVATE_PUB = STAGE + "/data/activation/type/pub/device/" + deviceId;
        TOPIC_TELEMETRY_PUB = STAGE + "/data/telemetry/type/pub/device/" + deviceId;
        TOPIC_NFC_PAYMENT_PUB = STAGE + "/organisation/" + orgId + "/data/payment/type/pub/device/" + deviceId;
        TOPIC_CONTENT_PUB = STAGE + "/organisation/" + orgId + "/data/adcontent/type/pub";

//        TOPIC_ACTIVATE_SUB = STAGE + "/data/activation/type/sub/device/" + deviceId;
        TOPIC_TELEMETRY_SUB = STAGE + "/data/telemetry/type/sub/device/" + deviceId;
        TOPIC_CONTENT_SUB = STAGE + "/organisation/" + orgId + "/data/adcontent/type/sub";
    }

    public enum MEDIALOOPSTATUS {START, STOP};

}
