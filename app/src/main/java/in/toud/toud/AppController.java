package in.toud.toud;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rpiyush on 15/8/15.
 */

public class AppController extends Application {
    private static AppController instance;
    private static Context appContext;

    private static final String SHARED_PREFERENCES_KEY = "in.toud.toud";
    private static final String SHARED_PREFERENCES_LOGIN_KEY = "in.toud.toud.login";
    private static final String SHARED_PREFERENCES_PASSWORD_KEY = "in.toud.toud.password";
    private static final String SHARED_PREFERENCES_SERVER_KEY = "in.toud.toud.server";


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        instance = this;
    }

    public AppController getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static void setUserSettings(String login, String password, String server) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit()
                .putString(SHARED_PREFERENCES_LOGIN_KEY, login)
                .putString(SHARED_PREFERENCES_PASSWORD_KEY, password)
                .putString(SHARED_PREFERENCES_SERVER_KEY, server)
                .apply();
    }

    public static String getLogin() {
        return getPreferences(MODE_PRIVATE).getString(SHARED_PREFERENCES_LOGIN_KEY, null);
    }

    public static String getPassword() {
        return getPreferences(MODE_PRIVATE).getString(SHARED_PREFERENCES_PASSWORD_KEY, null);
    }

    public static String getServer() {
        return getPreferences(MODE_PRIVATE).getString(SHARED_PREFERENCES_SERVER_KEY, null);
    }

    private static SharedPreferences getPreferences(int mode) {
        return getAppContext().getSharedPreferences(SHARED_PREFERENCES_KEY, mode);
    }
}