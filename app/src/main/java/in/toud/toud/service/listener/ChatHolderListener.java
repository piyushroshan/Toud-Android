package in.toud.toud.service.listener;

/**
 * Created by rpiyush on 15/8/15.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import in.toud.toud.AppController;
import in.toud.toud.model.JID;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by rpiyush on 15/8/15.
 */
public class ChatHolderListener implements ChatManagerListener, ChatMessageListener {


    private static final String DEBUG_TAG = ChatHolderListener.class.getSimpleName();
    private final ChatManager mChatManager;
    private final SparseArray<Chat> mChatSparseArray;


    public ChatHolderListener(XMPPConnection xmppConnection) {

        mChatSparseArray = new SparseArray<>();
        // setup chat manager
        mChatManager = ChatManager.getInstanceFor(xmppConnection);
        mChatManager.setMatchMode(ChatManager.MatchMode.SUPPLIED_JID);
        if (mChatManager != null) {
            mChatManager.addChatListener(this);
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.d(DEBUG_TAG, "call chatCreated");
        chat.addMessageListener(this);

        //TODO: add chat creation handler
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.d(DEBUG_TAG, "call processMessage");
        String threadID = chat.getThreadID();
        String participant = chat.getParticipant();
        String[] splitedParticipant = participant.split("/");
        int cid = getChatId(splitedParticipant[0], threadID);
        String jid = mChatSparseArray.get(cid).getParticipant().split("/")[0];
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        if (message.getType() == Message.Type.chat || message.getType() == Message.Type.normal) {
            if (message.getBody() != null) {
                saveToHistory(jid, myself.getUsername(), message.getBody(), true, false);
            }
            playRingtone();
        }
    }

    public int getChatId(String participant, String threadID) {
        int cid = -1;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        in.toud.toud.model.Chat myChat;
        Chat chat;
        RealmQuery<in.toud.toud.model.Chat> query = realm.where(in.toud.toud.model.Chat.class);
        if (threadID != null) {
            query.equalTo("participant", participant).equalTo("threadId", threadID);
            RealmResults<in.toud.toud.model.Chat> realmResults = query.findAllSorted("date", true);
            myChat = realmResults.first();
            if (myChat == null) {
                chat = mChatManager.createChat(participant, threadID, this);
            } else {
                cid = myChat.getChatId();
                chat = mChatSparseArray.get(cid);
                return cid;
            }

        } else {
            query.equalTo("participant", participant);
            RealmResults<in.toud.toud.model.Chat> realmResults = query.findAllSorted("date", true);
            myChat = realmResults.first();
            if (myChat == null) {
                chat = mChatManager.createChat(participant, this);
            } else {
                cid = myChat.getChatId();
                chat = mChatSparseArray.get(cid);
                return cid;
            }
        }
        if (myChat == null) {
            cid = mChatSparseArray.size();
            mChatSparseArray.append(cid, chat);
            myChat = realm.createObject(in.toud.toud.model.Chat.class);
            myChat.setChatId(cid);
            myChat.setParticipant(chat.getParticipant());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(myChat);
            realm.commitTransaction();
        }
        return myChat.getChatId();
    }

    public String getJid(int cid) {
        return mChatSparseArray.get(cid).getParticipant().split("/")[0];
    }

    public boolean sendMessage(int cid, String toUser, String message) {
        Log.d(DEBUG_TAG, "sendMessage");
        Chat chat = mChatSparseArray.get(cid);
        if (chat == null) {
            chat = mChatManager.createChat(toUser, this);
            mChatSparseArray.append(cid, chat);
            Realm realm = Realm.getInstance(AppController.getAppContext());
            in.toud.toud.model.Chat myChat = realm.createObject(in.toud.toud.model.Chat.class);
            myChat.setChatId(cid);
            myChat.setParticipant(chat.getParticipant().split("/")[0]);
            myChat.setTime(System.currentTimeMillis());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(myChat);
            realm.commitTransaction();
        }
        try {
            chat.sendMessage(message);

        } catch (SmackException.NotConnectedException e) {
            Log.e(DEBUG_TAG, "Exception occurred while sending message!", e);
            return false;
        }

        return true;
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

    public static void saveToHistory(String jidTo, String jidFrom, String message, boolean isSuccess, boolean isViewed) {
        Realm realm = Realm.getInstance(AppController.getAppContext());
        in.toud.toud.model.Message msg = realm.createObject(in.toud.toud.model.Message.class);
        msg.setTo(jidTo);
        msg.setFrom(jidFrom);
        msg.setMessage(message);
        msg.setIsRead(isViewed);
        msg.setIsSent(isSuccess);
        msg.setTime(System.currentTimeMillis());
        realm.beginTransaction();
        RealmResults<in.toud.toud.model.Message> myclassRealmResults = realm.where(in.toud.toud.model.Message.class).findAllSorted("id", false);
        long nextKey = myclassRealmResults.first().getId() + 1;
        msg.setId(nextKey);
        realm.copyToRealmOrUpdate(msg);
        realm.commitTransaction();
    }

}