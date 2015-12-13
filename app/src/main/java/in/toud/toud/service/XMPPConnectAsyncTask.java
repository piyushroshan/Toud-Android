package in.toud.toud.service;

/**
 * Created by rpiyush on 15/8/15.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import de.duenndns.ssl.MemorizingTrustManager;
import in.toud.toud.AppController;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Created by rpiyush on 15/8/15.
 */
public class XMPPConnectAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String DEBUG_TAG = XMPPConnectAsyncTask.class.getSimpleName();
    private static final int LOGIN_SUCCESS_CODE = 0;
    private static final int LOGIN_FAILED_CODE = 1;
    private ConnectionListener mConnectionListener = null;
    private AbstractXMPPConnection mConnection;
    //private final User mUser;

    public XMPPConnectAsyncTask(User user,
                                ConnectionListener connectionListener) {
        Log.d(DEBUG_TAG, "constructor");
        Realm realm = Realm.getInstance(AppController.getAppContext());
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
        mConnectionListener = connectionListener;
    }

    @Override
    protected Void doInBackground(Void[] params) {
        try {
            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            SSLContext sc = null;
            MemorizingTrustManager mtm = null;
            try {
                mtm = new MemorizingTrustManager(AppController.getAppContext());
                sc = SSLContext.getInstance("TLS");
                sc.init(null, new X509TrustManager[] { mtm }, new SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            } catch (KeyManagementException e) {
                throw new IllegalStateException(e);
            }
            configBuilder.setCustomSSLContext(sc);
            configBuilder.setHostnameVerifier(mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
            configBuilder.setDebuggerEnabled(true);

            Realm realm = Realm.getInstance(AppController.getAppContext());
            RealmQuery query = realm.where(User.class);
            RealmObject userRealmObject = query.findFirst();
            User mUser = (User) userRealmObject;

            configBuilder.setUsernameAndPassword(mUser.getUsername().split("@")[0], mUser.getPassword());
            configBuilder.setServiceName(mUser.getHostUrl());
            configBuilder.setResource("mobile");
            configBuilder.setHost(mUser.getHostUrl());
            configBuilder.setSendPresence(true);
            configBuilder.setPort(XMMPService.DEFAULT_XMPP_PORT);

            mConnection = new XMPPTCPConnection(configBuilder.build());
            // Connect to the server
            if (!mConnection.isConnected()) {
                Log.d(DEBUG_TAG, "starting connection");
                try {
                    mConnection.setPacketReplyTimeout(30000000);
                    mConnection.addConnectionListener(mConnectionListener);
                    mConnection.connect();
                } catch (SmackException | IOException | XMPPException e) {
                    String error = "Failed to connect!!\n Internet Connection error";
                    e.printStackTrace();
                    Log.d(DEBUG_TAG, e.getMessage());
                    mConnectionListener.connectionClosedOnError(e);
                }
            }
            // provide established connection to server
            //     Log.d(DEBUG_TAG, "connection is "+mConnection.isConnected());

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(DEBUG_TAG, e.getMessage());
            mConnectionListener.connectionClosedOnError(e);
        }
        return null;
    }

    public interface LoginEstablishedListener {
        void onLoginSuccess(int successCode);

        void onLoginFailed(int errorCode);
    }
}