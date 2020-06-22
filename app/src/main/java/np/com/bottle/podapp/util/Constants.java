package np.com.bottle.podapp.util;

public class Constants {
    public static final String SUCCESS = "success";
    public static final String KEYSTORE_NAME = "bottle_pod_keystore";
    public static final String KEYSTORE_PASSWORD = "?_Mb5XLc)K+Zk@Fxb6}T}KmdkT5m&`_<";
    public static final String CERTIFICATE_ID = "default";

    public static final int PORT = 8000;

    public static final String STAGE = "test";

    public static final String TOPIC_ACTIVATE = STAGE + "Activate";
    public static final String TOPIC_NFC_PAYMENT = STAGE + "Application";
    public static final String TOPIC_CONTENT_REQUEST = "stage/" + STAGE + "/org/5e56154c85da5800084e136a/content/request";
    public static final String TOPIC_CONTENT_RESPONSE = "stage/" + STAGE + "/org/5e56154c85da5800084e136a/content/response";

}
