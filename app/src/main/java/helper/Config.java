package helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by User on 12/08/2018.
 */

public class Config {
        public static final String WK_PREFS_NAME = "WK_PrefsFile";
        public static final String WK_PREFS_ID_VAL = "uuid";
        public static final String WK_PREFS_HOBBY = "hobby";
        public static final String WK_PREFS_FAVFOOD = "favfood";

        public static final int ALERT_DIALOG_FORGOTPWD = 1;
        public static final String OS_TYPE_ANDROID = "3";

        public static final String TYPE_FBLOGIN = "1";
        public static final String TYPE_GMSLOGIN = "2";
        public static final String TYPE_APP_LOGIN   = "3";

        public static final String FB_EMAIL = "email";

        public static final int RC_GMAIL_SIGN_IN = 007;

        public static final String PAGE_STATUS_LOGIN   = "3";
        public static final String WEBAPI_RESP_SUCCESS = "SUCCESS";

        public static final String WEEKUK_SUPPORT_EMAIL = "support@weekuk.com";
        public static final String WEEKUK_SUPPORTEMAIL_SUB = "Weekuk Help";
        public static final String WEEKUK_SUPPORTEMAIL_BODY = "";

        public static final String CURR_LOCATION_LAT= "LAT";
        public static final String CURR_LOCATION_LONG = "LON";
        public static final String USERPROF_TAG_HOBBYTAG = "HOBBY";
        public static final String USERPROF_TAG_FAVFOODTAG = "FAVFOOD";
        public static int LOCATION_GMAP = 2011;
        public static final int REQCODE_USERPROF_HOBBY = 1985;
        public static final int REQCODE_USERPROF_FAVFOOD = 1986;

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

        public String getSessionId(Context ctx) {
            return mSessionId;
        }

        public void setSessionId(String pSessionId) {
            this.mSessionId = pSessionId;
        }
    }

