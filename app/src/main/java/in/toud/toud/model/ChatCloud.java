package in.toud.toud.model;


import android.text.format.Time;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by rpiyush on 13/9/15.
 */
public class ChatCloud extends RealmObject {
    public static final String TAG = ChatCloud.class.getSimpleName();
    @PrimaryKey
    private String chatCloudTag;
    private long lastSent;
    private long lastRecieved;
    private long createdOn;
    private String support;

    public String getChatCloudTag() {
        return this.chatCloudTag;
    }

    public void setChatCloudTag(String chatCloudTag) {
        this.chatCloudTag = chatCloudTag;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public long getCreatedOn() {
        return this.createdOn;
    }

    public long getLastSent() {
        return this.lastSent;
    }

    public void setLastSent(long lastSent) {
        this.lastSent = lastSent;
    }

    public long getLastRecieved() {
        return this.lastRecieved;
    }

    public void setLastRecieved(long lastRecieved) {
        this.lastRecieved = lastRecieved;
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

}
