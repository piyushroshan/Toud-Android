package in.toud.toud.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import in.toud.toud.chat.CMessage;
import in.toud.toud.model.User;
import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.service.listener.ChatHolderListener;
//import in.toud.toud.service.listener.MUCHolderListener;
import in.toud.toud.service.listener.RosterHolderListener;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.IOException;

/**
 * Created by rpiyush on 15/8/15.
 */
public class XMMPService extends Service implements ConnectionListener {

    public static final String DEBUG_TAG = XMMPService.class.getSimpleName();

    public static final String AUTH_STATUS = "AUTH_STATUS";
    public static final String AUTH_STATUS_BROADCAST = "AUTH_STATUS_BROADCAST";


    public static final int DEFAULT_XMPP_PORT = 5222;
    private String mServer;

    private AbstractXMPPConnection mConnection;
    private Context mUIContext;
    private ChatHolderListener mChatHolderListener;
    //private MUCHolderListener                     mMultiUserChatHolder;
    private RosterHolderListener mRosterHolderListener;
    private XMPPConnectAsyncTask mXMPPTread;
    private ImageView mAvatarView;

    public void setAvatarView(ImageView avatarView) {
        mAvatarView = avatarView;
    }

    public ImageView getAvatarView() {
        return mAvatarView;
    }

    public void addRoster(String m) {
        if (mConnection != null) {
            mRosterHolderListener.createRoster(m);
        }
    }

    public class LocalBinder extends Binder {
        public XMMPService getService() {
            return XMMPService.this;
        }
    }

    private final IBinder localBinder = new LocalBinder();

    volatile private boolean authorize;
    private User mUser;


    public XMMPService() {
    }

    public boolean isAuthorize() {
        return authorize;
    }


    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "Creating XMMPService...");
        super.onCreate();
        Log.i(DEBUG_TAG, "XMPPService created.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onDestroy() {
        mRosterHolderListener.stopDBTask();
        super.onDestroy();
    }

    public void connect(User user) {
        mUser = user;
        mXMPPTread = new XMPPConnectAsyncTask(mUser, this);
        mXMPPTread.execute();
    }

    public void sendMessageToChat(int id, String message ) {

        SendMessageAsyncTask mSendMessageAsyncTask = new SendMessageAsyncTask();
        mSendMessageAsyncTask.execute(id, null, message, 0);

    }

    public void sendMessagesPending() {
        SendMessagesPendingAsyncTask mSendMessagesPendingAsyncTask = new SendMessagesPendingAsyncTask();
        mSendMessagesPendingAsyncTask.execute();
    }

    public void sendMessageToChat(String jid, String message) {

        SendMessageAsyncTask mSendMessageAsyncTask = new SendMessageAsyncTask();
        mSendMessageAsyncTask.execute(0, jid, message, 1);

    }

    public void sendStateToChat(String jid, ChatState state) {

        SendChatStateAsyncTask mSendChatStateAsyncTask = new SendChatStateAsyncTask();
        Log.d(DEBUG_TAG, jid + "SATETASK  : " + state.toString());
        mSendChatStateAsyncTask.execute(jid, state);
        //mChatHolderListener.sendChatStateMessage(jid, state);

    }

    /**
     * Send Notification to all Listeners that auth status changed
     *
     * @param isSuccess new status
     */
    private void sendAuthStatusToListeners(boolean isSuccess) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(AUTH_STATUS_BROADCAST);
        // You can also include some extra data.
        intent.putExtra(AUTH_STATUS, isSuccess);
        LocalBroadcastManager.getInstance(AppController.getAppContext()).sendBroadcast(intent);
    }

    /*public void createNewMUC(int roomJid, String roomName) {
        JoinToMUCAsyncTask joinToMUCAsyncTask = new JoinToMUCAsyncTask();
        joinToMUCAsyncTask.execute(roomJid, roomName);

    }*/

    public void loadAvatar(final ImageView avatarImageView, final String jid) {
        AsyncTask<Void, Void, Drawable> AsyncTaskasyncTask = new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... params) {
                VCardManager vCardManager = VCardManager.getInstanceFor(mConnection);

                Drawable image = null;
                try {
                    VCard vCard = vCardManager.loadVCard(jid);
                    byte[] avatar = vCard.getAvatar();
                    image = new BitmapDrawable(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));

                } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                    Log.e(DEBUG_TAG, e.getMessage());

                }
                return image;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                if (drawable != null) {
                    avatarImageView.setImageDrawable(drawable);
                }
            }
        };
        AsyncTaskasyncTask.execute();

    }


    @Override
    public void connected(final XMPPConnection connection) {
        Log.i(DEBUG_TAG, "connected");
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mConnection = (AbstractXMPPConnection) connection;
                try {
                    // Log into the server
                    authorize = mConnection.isAuthenticated();
                    if (!authorize) {

                        Log.d(DEBUG_TAG, "start authorization");
                        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

                        mConnection.login();
                        authorize = true;
                        //loadAvatar(mAvatarView,mUser.getJID());
                        Log.d(DEBUG_TAG, "authorization is " + mConnection.isAuthenticated() + " now");
                    } else {
                        // provide error to server
                        Log.d(DEBUG_TAG, "authorization was " + mConnection.isAuthenticated());
                    }

                } catch (XMPPException | SmackException | IOException e) {

                    Log.e(DEBUG_TAG, e.getMessage());
                    sendAuthStatusToListeners(false);
                    connect(mUser);
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.i(DEBUG_TAG, "authenticated");

        // Create listener for ChatManagerListener and ChatMessageListener
        mChatHolderListener = new ChatHolderListener(connection);
        mRosterHolderListener = new RosterHolderListener(connection);
        //mMultiUserChatHolder  = new MUCHolderListener(connection);
        sendAuthStatusToListeners(true);

    }


    @Override
    public void connectionClosed() {
        Log.i(DEBUG_TAG, "connectionClosed");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i(DEBUG_TAG, "connectionClosedOnError");
        sendAuthStatusToListeners(false);
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(DEBUG_TAG, "reconnectionSuccessful");
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.i(DEBUG_TAG, "reconnectingIn");
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Log into the server
                    authorize = mConnection.isAuthenticated();
                    if (!authorize) {

                        Log.d(DEBUG_TAG, "start authorization");
                        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

                        mConnection.login();
                        authorize = true;
                        Log.d(DEBUG_TAG, "authorization is " + mConnection.isAuthenticated() + " now");
                    } else {
                        // provide error to server
                        Log.d(DEBUG_TAG, "authorization was " + mConnection.isAuthenticated());
                    }

                } catch (XMPPException | SmackException | IOException e) {

                    Log.e(DEBUG_TAG, e.getMessage());
                    sendAuthStatusToListeners(false);
                    connect(mUser);
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.i(DEBUG_TAG, "reconnectionFailed");
    }


    private class SendMessageAsyncTask extends AsyncTask<Object, Integer, String> {
        /**
         * @param objects
         * @return
         */
        protected String doInBackground(Object... objects) {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            int cid = (int) objects[0];
            String jid = (String) objects[1];
            String message = (String) objects[2];
            int option = (int) objects[3];
            Log.d(DEBUG_TAG, " doInBackground with SendMessageAsyncTask");
            String resultMessage = "something goes wrong!";
            boolean isSuccess;

            if(option == 0) {
                resultMessage = sendMessageToSingleChat(cid, message);
            }else if(option == 1){
                resultMessage = sendMessageToSingleChat(jid, message);
            }

            return resultMessage;
        }

        private String sendMessageToSingleChat(int cid, String message) {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            boolean isSuccess;
            String jid = mChatHolderListener.getJid(cid);
            String defaultChatTread = getDefaultTread();
            isSuccess = mChatHolderListener.sendMessage(cid,
                    jid,
                    message);
            String resultMessage = "something goes wrong!";
            if (isSuccess) {
                resultMessage = "Message: " + message + " send to" + jid;
            }
            boolean isViewed = false;
            Realm realm = Realm.getInstance(AppController.getAppContext());
            RealmQuery query = realm.where(User.class);
            RealmObject userRealmObject = query.findFirst();
            User myself = (User) userRealmObject;
            String[] msg = message.split("#", 2);
            ChatHolderListener.saveToHistory(jid, myself.getUsername(), msg[0], msg[1], isSuccess, isViewed);
            return resultMessage;
        }

        private String sendMessageToSingleChat(String to, String message) {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            boolean isSuccess = false;
            String defaultChatTread = getDefaultTread();
            try {
                int cid = mChatHolderListener.getChatId(to, defaultChatTread);
                isSuccess = mChatHolderListener.sendMessage(to, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String resultMessage = "something goes wrong!";
            if (isSuccess) {
                resultMessage = "Message: " + message + " send to" + to;
            }
            boolean isViewed = false;
            Realm realm = Realm.getInstance(AppController.getAppContext());
            RealmQuery query = realm.where(User.class);
            RealmObject userRealmObject = query.findFirst();
            User myself = (User) userRealmObject;
            String[] msg = message.split("#", 2);
            ChatHolderListener.saveToHistory(to, myself.getUsername(), msg[0], msg[1], isSuccess, isViewed);
            return resultMessage;
        }

        /*private String sendMessageToMultiChat(int jid, String message) {
            boolean isSuccess;
            String defaultChatTread = getDefaultTread();
            isSuccess  = mMultiUserChatHolder.sendMessage(
                    message);
            String resultMessage = "something goes wrong!";
            if(isSuccess){
                resultMessage ="CMessage: "+ message +" send to"+ mMultiUserChatHolder.getRoomName();
            }
            boolean isViewed = true;
            ChatHolderListener.saveToHistory(jid, AppSQLiteOpenHelper.MY_JID_ID, message, isSuccess,isViewed);
            return resultMessage;
        }*/

        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute Chat " + result);
        }


        private String getDefaultTread() {
            return AppController.getAppContext().getResources().getString(R.string.new_chat);
        }


    }

    /*private class JoinToMUCAsyncTask extends AsyncTask<Object, Integer, String> {
        /**
         *
         * @param objects
         * @return
         */
        /*protected String doInBackground(Object... objects) {
            int roomJid =(lint)objects[0];
            String roomName =(String) objects[1];
            Log.d(DEBUG_TAG," doInBackground with SendMessageAsyncTask");
            String resultMessage = "something goes wrong!";
            // Join to default room
            boolean isJoinSuccess = mMultiUserChatHolder.joinToMUC(roomJid, roomName);

            if(isJoinSuccess){
                resultMessage ="mMultiUserChatHolder: chat created or joined: "+ mMultiUserChatHolder.getRoomJID();
            }

            return resultMessage;
        }


        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute " + result);
        }

    }*/

    private class SendChatStateAsyncTask extends AsyncTask<Object, Integer, String> {
        /**
         * @param objects
         * @return
         */
        protected String doInBackground(Object... objects) {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            Boolean isSuccess = true;
            String jid = (String) objects[0];
            ChatState state = (ChatState) objects[1];
            String result = "ChatState something wrong";
            Log.d(DEBUG_TAG, " doInBackground with SendChatStateAsyncTask User: " + jid + " State: " + state.toString() + " ");
            if (jid != null && state != null) {
                if (mChatHolderListener != null) {
                    mChatHolderListener.sendChatStateMessage(jid, state);
                    result = "User: " + jid + " State: " + state.toString();
                }
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute State " + result);
        }
    }

    private class SendMessagesPendingAsyncTask extends AsyncTask<Object, Integer, String> {
        /**
         * @param objects
         * @return
         */
        protected String doInBackground(Object... objects) {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            String result = sendMessagePending();
            return result;
        }

        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute State " + result);
        }

        private String sendMessagePending() {
            if (mConnection == null || !mConnection.isAuthenticated()) {
                return "Not Connected";
            }
            String defaultChatTread = getDefaultTread();
            String resultMessage = "Unknown";
            Realm realm = Realm.getInstance(AppController.getAppContext());
            RealmResults<in.toud.toud.model.Message> msgRealmResults = realm.where(in.toud.toud.model.Message.class)
                    .equalTo("isSent", false).findAllSorted("time");
            for (int i = 0; i < msgRealmResults.size(); i++) {
                boolean isSuccess = false;
                in.toud.toud.model.Message msg = (in.toud.toud.model.Message) msgRealmResults.get(i);
                try {
                    int cid = mChatHolderListener.getChatId(msg.getTo(), defaultChatTread);
                    isSuccess = mChatHolderListener.sendMessage(msg.getTo(), msg.getChatCloudTag() + msg.getMessage());
                    resultMessage = "something goes wrong!";
                    if (isSuccess) {
                        resultMessage = "Message: " + msg.getChatCloudTag() + msg.getMessage() + " send to" + msg.getTo();
                    }
                    msg.setIsSent(isSuccess);
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(msg);
                    realm.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                    resultMessage = e.toString();
                }
            }
            return resultMessage;
        }

        private String getDefaultTread() {
            return AppController.getAppContext().getResources().getString(R.string.new_chat);
        }
    }

}
