package np.com.bottle.podapp.nfc;

public class NfcAppKeys {

    public static final String LICENSE_KEY = "f37dbaed6f5e9e54a9880310139beeda";

    /**
     * 16 bytes AES128 Key.
     */
    public static final byte[] KEY_AES128_AllAccess = {(byte) 0x45, (byte) 0x28,
            (byte) 0x48, (byte) 0x2B, (byte) 0x4D, (byte) 0x62, (byte) 0x51,
            (byte) 0x65, (byte) 0x54, (byte) 0x68, (byte) 0x57, (byte) 0x6D,
            (byte) 0x5A, (byte) 0x71, (byte) 0x34, (byte) 0x74};

    /**
     * Only these types of Keys can be stored by the Helper class.
     */
    public enum EnumKeyType {
        EnumAESKey,
        EnumDESKey,
        EnumMifareKey
    }
}
