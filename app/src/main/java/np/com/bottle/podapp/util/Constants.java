package np.com.bottle.podapp.util;

public class Constants {
    public static final String SUCCESS = "success";
    public static final String KEYSTORE_NAME = "bottle_pod_keystore";
    public static final String KEYSTORE_PASSWORD = "?_Mb5XLc)K+Zk@Fxb6}T}KmdkT5m&`_<";
    public static final String CERTIFICATE_ID = "default";

    public static final int PORT = 8000;

    // region AWS Credentials & Key
    // Only for development purpose.
    static final String strCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDWTCCAkGgAwIBAgIUVTNodrcGg7FtuGK/zwXPYKxAEjQwDQYJKoZIhvcNAQEL\n" +
            "BQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\n" +
            "SW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIwMDMwMzA4MTcw\n" +
            "MVoXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\n" +
            "ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKEWcAGzybB6dy7NW3SH\n" +
            "IXA5VaWMUOkWWz6I5ZAfQA7GUZn5wUxQtSeJVC9WNiyDv0Cw8MCrRHbPjjSKhnx6\n" +
            "DWpYJE+hMjmpeDMKz+AUBylOnydNzRLI9a7+hDO/ekggIP5K4b7jf+6Lv+KGKFJN\n" +
            "v8DuY3bKlyL4nNm1MZunogRndOaWZwKbhwarJZQ0rSz7XjLi+iFHDgrKPgVhDpPI\n" +
            "LgfQRJII8rXYBkxD9XM5d/yiZ6Utdl3msF5QztOzwZlu9jzpJ/NhUjPQ5rlGhQGc\n" +
            "22wcOXDvkelbi3tDkgsDVxphcdb+UVhdMvU2UICsL1gJy10TlSodBjZOde3cdJFS\n" +
            "sucCAwEAAaNgMF4wHwYDVR0jBBgwFoAUXt2MnLC0bNR/ArCo30ppHKS6W3AwHQYD\n" +
            "VR0OBBYEFDeEZjJ+sLDErxhCNZssOTY5HuhKMAwGA1UdEwEB/wQCMAAwDgYDVR0P\n" +
            "AQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQDPt0A3d2S563A/SUAykp4hVj8y\n" +
            "4n5iMZSF5BjjtAQ1qdEAKGDIkOGvQsgDCoYHhIR8avgei6XM64L/8a2PhQP4og5E\n" +
            "FostBoiAZ4kBt/H3sOnB/a+2Tm9xsz6fywKFST9qn3yIciEiCyULnIB8ybibmRQx\n" +
            "4L1vVH9nBCgdz0X8h0y7eJGYYVcbAKL1H9b3GzV2l4+QJQPxfy93rD5gnI0K/fnd\n" +
            "XiBHeLthJj4HzAy7IB2tF4B3eSGuw/cr1/Ql8SRHVG5JE5aBE6XJveqRSMy8IX7e\n" +
            "QH1FyO7QnImlGlPsufloTR4r4OYUf0og+RWwbNzJLGe5j+VoJ12o9sG3Mfx+\n" +
            "-----END CERTIFICATE-----";

    static final String strPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAoRZwAbPJsHp3Ls1bdIchcDlVpYxQ6RZbPojlkB9ADsZRmfnB\n" +
            "TFC1J4lUL1Y2LIO/QLDwwKtEds+ONIqGfHoNalgkT6EyOal4MwrP4BQHKU6fJ03N\n" +
            "Esj1rv6EM796SCAg/krhvuN/7ou/4oYoUk2/wO5jdsqXIvic2bUxm6eiBGd05pZn\n" +
            "ApuHBqsllDStLPteMuL6IUcOCso+BWEOk8guB9BEkgjytdgGTEP1czl3/KJnpS12\n" +
            "XeawXlDO07PBmW72POkn82FSM9DmuUaFAZzbbBw5cO+R6VuLe0OSCwNXGmFx1v5R\n" +
            "WF0y9TZQgKwvWAnLXROVKh0GNk517dx0kVKy5wIDAQABAoIBACdAUWCIz29aF+pm\n" +
            "jY9SUfqHbfAdxaVWFjuhndZFVxWCi2WqbshKrimJE+NI7YBhRdvcgX1g/hi5MFxr\n" +
            "GKQCFXzBJ2/jxlo4ihpd2xMtriUJjExGb97Q6tSOHeaCFKi4c3GjC2i5BHY6xTHM\n" +
            "IM0VNtErBT9v0+3oiEegGAtmiFrr+5ng22B/I9sy5tlmOcBQdZjokx/Q20ZraeMa\n" +
            "1GcVxzrQRcJOPJYZhB0dGuI8XzkTSteHxrybMeF8mFjtZqDG37TX48TiR8ukfEgn\n" +
            "AXVwylVW3pjqMWnubfOv4gN+wO/DB8A0+9Q6mLjUwX14m+zQGofFy/rSpF1kc+uO\n" +
            "E5ivkfkCgYEAz7AoYNuxYSgobUPILVbNywy3EZRFER6r/O5IOTMshjn12zg42UAp\n" +
            "t+AzNzHak4JNEJOE+Y9o2WzF7pu4IGmxX5B1t/gyAIymjjQNZt+Uti1DtHlNZ8I8\n" +
            "fTsYRZRMR3+qWq5knTw/+lDgiRzHRQBHHvikaAOS891Wc1Pn/f/MitMCgYEAxo83\n" +
            "7l9NuirGNcCbpPCcIFxeqYbFoSwSMaj32Xd3JdCWIEWhPg2fIzVU9JRDYDbmAXdJ\n" +
            "AJNm9Jso1SICJTcnqIlp22ixNS7dJDNpeT8PYQ4n/IOvJQE+D8WQ9NkRwf1e8Yen\n" +
            "HvguhQHN9WVNp+92P3J8/8XQYYIjtLC4FndPgx0CgYEAgX1Gn6U6QeZE1mNFgqc1\n" +
            "Zh0GYyp7qYHjH679TtSONR4cINM79pAfsqpRpF8r9X5vSB+B8AsvByjfe7aRJXxg\n" +
            "f8HU86AGpPsMuWrQ1EwgLzmnlfVvgnrDNh1LM86ThJyffSxwROyUwx/77uEHiQj0\n" +
            "TuYds1BPJkbhKMSux1nekMsCgYEAgUmmAJebs9nAQCXO8sIc1tFubfIsYvqgSiec\n" +
            "YsRwF3ZPGmpcSFx0P2etjqevi0mT0GaMaUyRYDpbhxPi07qrHwmCnPP5VwjRw42a\n" +
            "YvFVoTune7t+4piBgaGB9nwVHPxOwvQyNG8jU0XOhrfUrcG1fDCfBagXPDaR60ia\n" +
            "RT6gzl0CgYBie7OWZm5MS9pr5CS+XKpDH7ZCO6ym0WKU/MToGOB9JUKbYbsMbiKh\n" +
            "4iQX6Mp0yJ1/IcFvtPHNcNCHHiwfKKQEjB7bRHZiwJP9cFRQDx/XpQMoWsuf32Ae\n" +
            "56d9bwjyHUgCZb5nB16gE2jR+dhvK+9okyS59Ep2vX3dYuegRJBfbA==\n" +
            "-----END RSA PRIVATE KEY-----\n";

    static final String strPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoRZwAbPJsHp3Ls1bdIch\n" +
            "cDlVpYxQ6RZbPojlkB9ADsZRmfnBTFC1J4lUL1Y2LIO/QLDwwKtEds+ONIqGfHoN\n" +
            "algkT6EyOal4MwrP4BQHKU6fJ03NEsj1rv6EM796SCAg/krhvuN/7ou/4oYoUk2/\n" +
            "wO5jdsqXIvic2bUxm6eiBGd05pZnApuHBqsllDStLPteMuL6IUcOCso+BWEOk8gu\n" +
            "B9BEkgjytdgGTEP1czl3/KJnpS12XeawXlDO07PBmW72POkn82FSM9DmuUaFAZzb\n" +
            "bBw5cO+R6VuLe0OSCwNXGmFx1v5RWF0y9TZQgKwvWAnLXROVKh0GNk517dx0kVKy\n" +
            "5wIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";
    // endregion
}
