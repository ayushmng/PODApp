package np.com.bottle.podapp;

import android.app.Application;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;

import java.security.KeyStore;

public class PODApp extends Application {

    private static String TAG = PODApp.class.getSimpleName();

//    KeyStore clientKeyStore = null;

    @Override
    public void onCreate() {
        super.onCreate();


    }

//    private void initializeAwsIot() {
//        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
//                keystorePath, keystoreName, keystorePassword);
//    }
}
