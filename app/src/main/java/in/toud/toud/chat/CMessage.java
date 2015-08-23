package in.toud.toud.chat;

import in.toud.toud.AppController;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by rpiyush on 15/8/15.
 */
public class CMessage {
    /**
     * The content of the message
     */
    String message;
    /**
     * boolean to determine, who is sender of this message
     */
    boolean isMine;
    /**
     * boolean to determine, whether the message is a status message or not.
     * it reflects the changes/updates about the sender is writing, have entered text etc
     */
    boolean isStatusMessage;

    /**
     * Constructor to make a CMessage object
     */
    public CMessage(String message, String from) {
        super();
        this.message = message;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        if (myself.getUsername() == from) {
            this.isMine = true;
        }else{
            this.isMine = false;
        }
        this.isStatusMessage = false;
    }
    public CMessage(String message, boolean isMine) {
        super();
        this.message = message;
        this.isMine = isMine;
        this.isStatusMessage = false;
    }
    /**
     * Constructor to make a status CMessage object
     * consider the parameters are swaped from default CMessage constructor,
     *  not a good approach but have to go with it.
     */
    public CMessage(boolean status, String message) {
        super();
        this.message = message;
        this.isMine = false;
        this.isStatusMessage = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isMine() {
        return isMine;
    }
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
    public boolean isStatusMessage() {
        return isStatusMessage;
    }
    public void setStatusMessage(boolean isStatusMessage) {
        this.isStatusMessage = isStatusMessage;
    }


}