package Helper;

/**
 * Created by User on 12/08/2018.
 */

public class Config {
        public static final String OS_TYPE_ANDROID = "3";

        public static final String TYPE_FBLOGIN = "1";
        public static final String TYPE_GMSLOGIN = "2";
        public static final String TYPE_APP_LOGIN   = "3";

        public static final String FB_EMAIL = "email";

        public static final int RC_GMAIL_SIGN_IN = 007;

        private static Config config = null;

        private String mSessionId;

        private Config() {
        }

        public static Config getInstance() {
            if (config == null) {
                config = new Config();
            }
            return config;
        }

        public String getSessionId() {
            return mSessionId;
        }

        public void setSessionId(String pSessionId) {
            this.mSessionId = pSessionId;
        }
    }

