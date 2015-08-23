/*
package in.toud.toud.service.listener;


import android.content.ContentValues;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import in.toud.toud.xmppchat.AppController;
import in.toud.toud.xmppchat.R;
import in.toud.toud.xmppchat.activity.MainActivity;
import in.toud.toud.xmppchat.db.AppContentProvider;
import in.toud.toud.xmppchat.db.AppSQLiteOpenHelper;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;

import java.util.HashMap;
import java.util.List;

import static in.toud.toud.xmppchat.db.AppContentProvider.CONTENT_URI_MESSAGE;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.JID_COLUMN_GROUP_NAME;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.JID_COLUMN_LOGIN;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.JID_COLUMN_NAME;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.JID_COLUMN_TYPE;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_CHAT_ID;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_DATE;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_MESSAGE;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_RECEIVED;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_USER_ID;
import static in.toud.toud.xmppchat.db.AppSQLiteOpenHelper.MESSAGE_VIEWED;

*/
/**
 * Created by rpiyush on 15/8/15.
 *
 * @param messages array of message which have to proccessed
 * @return
 * @param messages array of message which have to proccessed
 * @return
 * @param messages array of message which have to proccessed
 * @return
 *//*

public class MUCHolderListener implements   InvitationListener, MessageListener, PresenceListener {


    private static final String            DEBUG_TAG = MUCHolderListener.class.getSimpleName();

    private final        MultiUserChatManager mChatManager;
    private final        HashMap<String,Long> mNickToJidMap;


    private long          mRoomJID;
    private String        mRoomName;
    private MultiUserChat multiUserChat;
    private String        mMyNick;


    public MUCHolderListener(XMPPConnection xmppConnection) {

        mNickToJidMap = new HashMap<>();
        // setup chat manager
        mChatManager = MultiUserChatManager.getInstanceFor(xmppConnection);
        if (mChatManager != null) {
            mChatManager.addInvitationListener(this);
        }
    }


    public boolean joinToMUC(long roomJid, String room){
        Log.i(DEBUG_TAG, "joinToMUC: try to join multiuser chat");
        multiUserChat = mChatManager.getMultiUserChat(room+"@conference.jabber.ru");
        mRoomName     = room;
        mRoomJID      = roomJid;
        mMyNick       = "Fesswood";
        if(!multiUserChat.isJoined()){
            try {
                multiUserChat.createOrJoin(mMyNick);
                saveNewMultiChatUser(multiUserChat);

            } catch (XMPPException.XMPPErrorException |SmackException e) {
                Log.e(DEBUG_TAG,"joinToMUC failed",e);
            }
        }else {
            MainActivity.showSnackbar(R.string.already_joined_to_room);
        }
        multiUserChat.addParticipantListener(this);
        multiUserChat.addMessageListener(this);
        Log.i(DEBUG_TAG, "joinToMUC: you has been joined to " + room);

        return true;
    }
    private void saveNewMultiChatUser(MultiUserChat multiUserChat) {
        List<String> occupants = multiUserChat.getOccupants();

        ContentValues contentValues = new ContentValues();
        for (String occupant:occupants){
            Occupant occupantProfile = multiUserChat.getOccupant(occupant);
            String nick = occupantProfile.getNick();
            if(nick !=mMyNick){
                contentValues.put(JID_COLUMN_NAME, nick);

                contentValues.put(JID_COLUMN_LOGIN, nick);
                contentValues.put(JID_COLUMN_TYPE,AppSQLiteOpenHelper.JID_TYPE_NICK);
                contentValues.put(JID_COLUMN_GROUP_NAME, "No group");

                Uri insert = AppController
                        .getAppContext()
                        .getContentResolver()
                        .insert(AppContentProvider.CONTENT_URI_JID, contentValues);
                Long Jid = new Long(insert.getLastPathSegment());
                mNickToJidMap.put(nick, Jid);
            }
        }

    }

    public static void saveToHistory(long jidTo,long jidFrom, String message, boolean isSuccess, boolean isViewed) {
        ContentValues values = new ContentValues();
        values.put(MESSAGE_CHAT_ID,jidTo);
        values.put(MESSAGE_DATE,System.currentTimeMillis());
        values.put(MESSAGE_USER_ID, jidFrom);
        values.put(MESSAGE_MESSAGE, message);
        values.put(MESSAGE_RECEIVED,isSuccess?1:0);
        values.put(MESSAGE_VIEWED,isViewed?1:0);
        AppController.getAppContext().getContentResolver()
                .insert(CONTENT_URI_MESSAGE, values);
    }

    @Override
    public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, CMessage message) {

    }

    @Override
    public void processMessage(CMessage message) {
        Log.i(DEBUG_TAG,"processMessage"+message);
        ProcessMessageAsyncTask processMessageAsyncTask =new ProcessMessageAsyncTask();
        processMessageAsyncTask.execute(message);
        playRingtone();
    }

    private void playRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(AppController.getAppContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getRoomJID() {
        return mRoomJID;
    }

    public boolean sendMessage(String message) {
        try {
            multiUserChat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            Log.e(DEBUG_TAG,"sendMessage failed:",e);
            return false;
        }
        return true;
    }

    public String getRoomName() {
        return mRoomName;
    }

    @Override
    public void processPresence(Presence presence) {
        ContentValues contentValues = new ContentValues();
        String nick = presence.getFrom().split("/")[1];
        if(nick !=mMyNick){
            saveNewUserNick(nick);
        }
    }


    private long saveNewUserNick(String nick) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(JID_COLUMN_NAME, nick);

        contentValues.put(JID_COLUMN_LOGIN, nick);
        contentValues.put(JID_COLUMN_TYPE, AppSQLiteOpenHelper.JID_TYPE_NICK);
        contentValues.put(JID_COLUMN_GROUP_NAME, "No group");

        Uri insert = AppController
                .getAppContext()
                .getContentResolver()
                .insert(AppContentProvider.CONTENT_URI_JID, contentValues);
        Long Jid = new Long(insert.getLastPathSegment());
        mNickToJidMap.put(nick, Jid);
        return Jid;
    }

    private class ProcessMessageAsyncTask extends AsyncTask<CMessage, Integer, String> {
        */
/**
 *
 * @param messages array of message which have to proccessed
 * @return
 *//*

        protected String doInBackground(CMessage... messages) {
            CMessage message=messages[0];
            Log.i(DEBUG_TAG,"ProcessMessageAsyncTask "+message);
            String nick          = message.getFrom().split("/")[1];
            String resultMessage = "something goes wrong!";
            if(!nick.equals(mMyNick)){

                Long jid=null;
                if (message.getType() == CMessage.Type.groupchat||message.getType() == CMessage.Type.normal) {
                    String[] split = message.getFrom().split("/");
                    jid = mNickToJidMap.get(split[1]);

                }
                if(jid==null){

                    jid = saveNewUserNick(nick);
                }

                if (message.getBody() != null) {
                    saveToHistory(mRoomJID, jid, message.getBody(), true, false);
                }

            }
            resultMessage="message proccessed";
            return resultMessage;
        }


        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute " + result);
        }

    }


}*/
