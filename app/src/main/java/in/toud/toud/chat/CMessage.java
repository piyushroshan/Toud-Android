package in.toud.toud.chat;

import android.util.Log;

import in.toud.toud.AppController;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by rpiyush on 15/8/15.
 */
public class CMessage {
    private static final String DEBUG_TAG = CMessage.class.getSimpleName();
    /**
     * The content of the message
     */
    String chatCloudTag;
    String message;
    /**
     * boolean to determine, who is sender of this message
     */
    boolean isMine;
    /**
     * boolean to determine, whether the message is a status message or not.
     * it reflects the changes/updates about the sender is writing, have entered chat_text etc
     */
    boolean isStatusMessage;

    /**
     * Constructor to make a CMessage object
     */
    public CMessage(String chatCloudTag, String message, String from) {
        super();
        this.message = message;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        Log.d(DEBUG_TAG, "CMessage " + from + " to " + myself.getUsername());
        if (myself.getUsername().equals(from)) {
            this.isMine = true;
        }else{
            this.isMine = false;
        }
        this.isStatusMessage = false;
        this.chatCloudTag = chatCloudTag;
    }
    /**
     * Constructor to make a status CMessage object
     * consider the parameters are swaped from default CMessage constructor,
     *  not a good approach but have to go with it.
     */
    public CMessage(String chatCloudTag, String message, boolean isMine, boolean status) {
        super();
        this.message = message;
        this.chatCloudTag = chatCloudTag;
        this.isMine = isMine;
        this.isStatusMessage = status;
    }
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isMine() {
        return this.isMine;
    }
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
    public boolean isStatusMessage() {
        return this.isStatusMessage;
    }
    public void setStatusMessage(boolean isStatusMessage) {
        this.isStatusMessage = isStatusMessage;
    }

    public String getChatCloudTag() {
        return this.chatCloudTag;
    }

    public void setChatCloudTag(String chatCloudTag) {
        this.chatCloudTag = chatCloudTag;
    }

    public CMessage(String message, boolean isMine) {
        super();
        this.message = message;
        this.isMine = isMine;
        this.isStatusMessage = true;
    }


}