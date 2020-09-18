package np.com.bottle.podapp.util;

import java.util.ArrayList;

public class Constants {
    public static final String SUCCESS = "success";
    public static final String KEYSTORE_NAME = "bottle_pod_keystore";
    public static final String KEYSTORE_PASSWORD = "?_Mb5XLc)K+Zk@Fxb6}T}KmdkT5m&`_<";
    public static final String CERTIFICATE_ID = "default";
    public static final String ALIAS_KEY_AES128 = "key_aes_128";

    public static final int PORT = 8000;

    public static final String STAGE_NAME = "dev";
    public static final String STAGE = "stage/" + STAGE_NAME;

    // Payload Types
    public static String PAYLOAD_TYPE_COMMAND = "command";
    public static String PAYLOAD_TYPE_HEALTH = "health";

    // Publish Topics
    public static String TOPIC_ACTIVATE_PUB;
    public static String TOPIC_TELEMETRY_PUB;
    public static String TOPIC_NFC_PAYMENT_PUB;
    public static String TOPIC_CONTENT_PUB;
    public static String TOPIC_ANDROID_PUB;

    // Subscribe Topics
    public static String TOPIC_TELEMETRY_SUB;
    public static String TOPIC_CONTENT_SUB;
    public static String TOPIC_ANDROID_SUB;

    public static final int PERMISSIONS_REQUEST_CODE = 1240;

    public static final int ITEMS_PER_PAGE = 6;

    // Identifies Card Recognition for Entrance or Payment
    public static boolean IS_ENTRANCE_VERIFICATION = false;
    public static boolean IS_CARD_INVALID = false;
    public static boolean IS_CARD_EXPIRED = false;

    public static void constructTopic(String orgId, String deviceId) {
        TOPIC_ACTIVATE_PUB = STAGE + "/data/activation/type/pub/device/" + deviceId;
        TOPIC_TELEMETRY_PUB = STAGE + "/data/telemetry/type/pub/device/" + deviceId;
        TOPIC_NFC_PAYMENT_PUB = STAGE + "/organisation/" + orgId + "/data/payment/type/pub/device/" + deviceId;
        TOPIC_CONTENT_PUB = STAGE + "/organisation/" + orgId + "/data/adcontent/type/pub";
        TOPIC_ANDROID_PUB = STAGE + "/data/android/type/pub/device/" + deviceId;

        TOPIC_TELEMETRY_SUB = STAGE + "/data/telemetry/type/sub/device/" + deviceId;
        TOPIC_CONTENT_SUB = STAGE + "/organisation/" + orgId + "/data/adcontent/type/sub";
        TOPIC_ANDROID_SUB = STAGE + "/data/android/type/sub/device/" + deviceId;
    }

    public enum MEDIALOOPSTATUS {START, STOP};

}


