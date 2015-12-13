package in.toud.toud.service.listener;

/**
 * Created by rpiyush on 15/8/15.
 */

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import de.halfbit.tinybus.TinyBus;
import in.toud.toud.AppController;
import in.toud.toud.chat.CMessage;
import in.toud.toud.chat.ChatCloudM;
import in.toud.toud.events.ChatCloudRecievedEvent;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

/**
 * Created by rpiyush on 15/8/15.
 */
public class ChatHolderListener implements ChatManagerListener, ChatMessageListener, ChatStateListener {


    private static final String DEBUG_TAG = ChatHolderListener.class.getSimpleName();
    private final ChatManager mChatManager;
    private final SparseArray<Chat> mChatSparseArray;
    private final XMPPConnection connection;
    private final VCardManager mVCardManager;
    private final ChatStateManager mChatStateManager;

    public ChatHolderListener(XMPPConnection xmppConnection) {

        mChatSparseArray = new SparseArray<>();
        // setup chat manager
        mChatManager = ChatManager.getInstanceFor(xmppConnection);
        mChatManager.setMatchMode(ChatManager.MatchMode.SUPPLIED_JID);
        if (mChatManager != null) {
            mChatManager.addChatListener(this);
        }
        mChatStateManager = ChatStateManager.getInstance(xmppConnection);
        this.connection = xmppConnection;
        mVCardManager = VCardManager.getInstanceFor(xmppConnection);
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.d(DEBUG_TAG, "call chatCreated");
        chat.addMessageListener(this);

        //TODO: add chat creation handler
    }


    @Override
    public void processMessage(Chat chat, Message message) {
        CMessage cMessage;
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
                Log.d(DEBUG_TAG, message.toString());
                String[] mMessage = message.getBody().split("#", 2);
                playRingtone();
                cMessage = new CMessage(mMessage[0], mMessage[1], jid);
                ChatCloud chatCloud = realm.where(ChatCloud.class).equalTo("chatCloudTag", mMessage[0]).findFirst();
                Boolean notify = false;
                if (chatCloud == null) {
                    chatCloud = new ChatCloud();
                    chatCloud.setCreatedOn(System.currentTimeMillis());
                    chatCloud.setChatCloudTag(mMessage[0]);
                    notify = true;
                }
                realm.beginTransaction();
                chatCloud.setLastRecieved(System.currentTimeMillis());
                chatCloud.setSupport(jid);
                realm.copyToRealmOrUpdate(chatCloud);
                realm.commitTransaction();
                if (notify)
                    TinyBus.from(AppController.getAppContext()).post(new ChatCloudRecievedEvent(new ChatCloudM(chatCloud.getChatCloudTag())));
                saveToHistory(myself.getUsername(), jid, mMessage[0], mMessage[1], true, false);
            } else {
                String msg;
                ChatStateExtension chatStateExtension = (ChatStateExtension) message.getExtension("http://jabber.org/protocol/chatstates");
                ChatState chatState = chatStateExtension.getChatState();
                String name;
                try {
                    VCard mVCard = mVCardManager.loadVCard(jid);
                    name = mVCard.getNickName();
                } catch (Exception e) {
                    //e.printStackTrace();
                    name = participant.split("@")[0];
                }

                if (ChatState.composing.equals(chatState)) {
                    msg = name + " is typing..";
                } else if (ChatState.gone.equals(chatState)) {
                    msg = name + " has left the conversation.";
                } else {
                    msg = name + ": " + chatState.name();
                }
                Log.d(DEBUG_TAG, chatStateExtension.getChatState().toString());
                cMessage = new CMessage(msg, false);
            }
        } else {
            Log.d(DEBUG_TAG, message.toString());
            String mMessage = message.getBody();
            cMessage = new CMessage(mMessage, false);
        }
        TinyBus.from(AppController.getAppContext()).post(new MessageRecievedEvent(cMessage));
    }

    @Override
    public void stateChanged(Chat chat, ChatState chatState) {
        Log.d(DEBUG_TAG, "call stateChange");
        String msg;
        String threadID = chat.getThreadID();
        String participant = chat.getParticipant();
        String[] splitedParticipant = participant.split("/");
        int cid = getChatId(splitedParticipant[0], threadID);
        if (ChatState.composing.equals(chatState)) {
            msg = participant.split("@")[0] + " is typing..";
        } else if (ChatState.gone.equals(chatState)) {
            msg = participant.split("@")[0] + " has left the conversation.";
        } else {
            msg = participant.split("@")[0] + ": " + chatState.name();
        }
        CMessage cMessage = new CMessage(msg, false);
        TinyBus.from(AppController.getAppContext()).post(new MessageRecievedEvent(cMessage));
    }

    public int getChatId(String participant, String threadID) {
        int cid = -1;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        in.toud.toud.model.Chat myChat;
        Chat chat;
        RealmQuery<in.toud.toud.model.Chat> query = realm.where(in.toud.toud.model.Chat.class);
        if (threadID != null) {
            query.equalTo("participant", participant).equalTo("threadId", threadID);
            RealmResults<in.toud.toud.model.Chat> realmResults = query.findAllSorted("time", true);
            if (realmResults.size() > 0){
                myChat = realmResults.first();
            }else{
                myChat = null;
            }
            if (myChat == null) {
                chat = mChatManager.getThreadChat(threadID);
                if (chat == null){
                    chat = mChatManager.createChat(participant, threadID, this);
                }

            } else {
                cid = myChat.getChatId();
                chat = mChatSparseArray.get(cid);
            }

        } else {
            query.equalTo("participant", participant);
            RealmResults<in.toud.toud.model.Chat> realmResults = query.findAllSorted("time", true);
            if (realmResults.size() > 0){
                myChat = realmResults.first();
            }else{
                myChat = null;
            }
            if (myChat == null) {
                chat = mChatManager.createChat(participant, this);
            } else {
                cid = myChat.getChatId();
                chat = mChatSparseArray.get(cid);
            }
        }
        if (myChat == null) {
            cid = mChatSparseArray.size();
            mChatSparseArray.append(cid, chat);
            myChat = new in.toud.toud.model.Chat();
            myChat.setThreadId(chat.getThreadID());
            myChat.setChatId(cid);
            myChat.setParticipant(chat.getParticipant().split("/")[0]);
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
            in.toud.toud.model.Chat myChat = new in.toud.toud.model.Chat();
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

    public boolean sendMessage(String toUser, String message) {
        Log.d(DEBUG_TAG, "sendMessage");
        int cid = getChatId(toUser, null);
        Chat chat = mChatSparseArray.get(cid);
        if (chat == null) {
            chat = mChatManager.createChat(toUser, this);
            mChatSparseArray.append(cid, chat);
            Realm realm = Realm.getInstance(AppController.getAppContext());
            in.toud.toud.model.Chat myChat = new in.toud.toud.model.Chat();
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

    public boolean sendChatStateMessage(String toUser, ChatState state) {
        Log.d(DEBUG_TAG, "sendChatState");
        int cid = getChatId(toUser, null);
        Chat chat = mChatSparseArray.get(cid);
        if (chat == null) {
            chat = mChatManager.createChat(toUser, this);
            mChatSparseArray.append(cid, chat);
            Realm realm = Realm.getInstance(AppController.getAppContext());
            in.toud.toud.model.Chat myChat = new in.toud.toud.model.Chat();
            myChat.setChatId(cid);
            myChat.setParticipant(chat.getParticipant().split("/")[0]);
            myChat.setTime(System.currentTimeMillis());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(myChat);
            realm.commitTransaction();
        }
        try {
            mChatStateManager.setCurrentState(state, chat);
        } catch (SmackException.NotConnectedException e) {
            Log.e(DEBUG_TAG, "Exception occurred while sending chatState!", e);
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

    public static void saveToHistory(String jidTo, String jidFrom, String chatCloudTag, String message, boolean isSuccess, boolean isViewed) {
        long nextKey;
        ChatCloud chatCloud;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        in.toud.toud.model.Message msg = new in.toud.toud.model.Message();
        msg.setTo(jidTo);
        msg.setFrom(jidFrom);
        msg.setMessage(message);
        msg.setIsRead(isViewed);
        msg.setIsSent(isSuccess);
        msg.setTime(System.currentTimeMillis());
        msg.setChatCloudTag(chatCloudTag);
        RealmResults<in.toud.toud.model.Message> myclassRealmResults = realm.where(in.toud.toud.model.Message.class).findAllSorted("id", false);
        RealmResults<ChatCloud> chatCloudRealmResults = realm.where(ChatCloud.class).equalTo("chatCloudTag", chatCloudTag).findAll();
        realm.beginTransaction();
        if (chatCloudRealmResults.size() > 0) {
            chatCloud = chatCloudRealmResults.first();
            chatCloud.setLastSent(System.currentTimeMillis());
            chatCloud.setLastRecieved(System.currentTimeMillis());
            realm.copyToRealmOrUpdate(chatCloud);
        }
        if (myclassRealmResults.size() > 0){
          nextKey = myclassRealmResults.first().getId() + 1;
        }else{
            nextKey = 1;
        }
        msg.setId(nextKey);
        realm.copyToRealmOrUpdate(msg);
        realm.commitTransaction();
    }

}