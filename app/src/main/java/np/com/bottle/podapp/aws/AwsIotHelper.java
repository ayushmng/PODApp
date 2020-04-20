package np.com.bottle.podapp.aws;

import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.amazonaws.services.iot.model.KeyPair;

import java.security.KeyStore;

import np.com.bottle.podapp.util.Constants;

public class AwsIotHelper {

    private static String TAG = AwsIotHelper.class.getSimpleName();

    /**
     * This is the method to save the AWS certificate, private key & public in keystore
     * Keystore name & password are pre-set in Constants.java file.
     * @param endpoint -> AWS IoT ARN
     * @param certificateId -> Certificate and key aliases in the KeyStore
     * @param cert -> unique certificate key for each device. Generate by Device Management platform.
     * @param privateKey -> unique private key for each device. Generate by Device Management platform.
     * @param publicKey -> public key. Generate by Device Management platform.
     * @param keystorePath -> Path where the keystore is saved.
     * @return -> returns "success" if the aws config is saved.
     */
    public String saveConfiguration(String endpoint, String certificateId, String cert, String privateKey, String publicKey, String keystorePath) {
        try {
            CreateKeysAndCertificateResult createKeysAndCertificateResult = new CreateKeysAndCertificateResult();
            createKeysAndCertificateResult.setCertificateArn(endpoint);
            createKeysAndCertificateResult.setCertificateId(certificateId);
            createKeysAndCertificateResult.setCertificatePem(cert);

            KeyPair keyPair = new KeyPair();
            keyPair.setPrivateKey(privateKey);
            keyPair.setPublicKey(publicKey);

            createKeysAndCertificateResult.setKeyPair(keyPair);

            AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                    createKeysAndCertificateResult.getCertificatePem(),
                    createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                    keystorePath, Constants.KEYSTORE_NAME, Constants.KEYSTORE_PASSWORD);

            return Constants.SUCCESS;
        } catch (Exception e) {
            return "" + e.getMessage();
        }
    }

    /**
     * This method is for initializing AWS IoT MQTT client. Necessary details can be obtained from Device Provisioning platform.
     * @param clientId -> Provisioned device's mqtt Id
     * @param endpointArn -> AWS IoT endpoint
     * @param keystorePath -> Path where the keystore is saved.
     * @return -> returns an instance of AWSIotMqttManager if successful connection to AWS IoT.
     */
    private static AWSIotMqttManager initializeAwsIot(String clientId, String endpointArn, String keystorePath) {
        try {
            KeyStore clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(Constants.CERTIFICATE_ID,
                    keystorePath, Constants.KEYSTORE_NAME, Constants.KEYSTORE_PASSWORD);

            AWSIotMqttManager mqttManager = new AWSIotMqttManager(clientId, endpointArn);

            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    Log.d(TAG, "Status = " + String.valueOf(status));
                }
            });

            return mqttManager;
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to AWS IoT.");
            Log.e(TAG, "Exception : " + e.getMessage());
            return null;
        }
    }
}
