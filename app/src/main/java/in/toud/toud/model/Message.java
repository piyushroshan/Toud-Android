package in.toud.toud.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by rpiyush on 15/8/15.
 */
public class Message extends RealmObject {

    public static final String TAG = Message.class.getSimpleName();
    @PrimaryKey
    private long id;
    private String message;
    private boolean isRead;
    private boolean isSent;
    private String from;
    private String to;
    private long time;
    private String chatCloudTag;

    public String getChatCloudTag() {
        return this.chatCloudTag;
    }

    public void setChatCloudTag(String cloud) {
        this.chatCloudTag = cloud;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return this.from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return this.to;
    }

    public boolean getIsSent() {
        return this.isSent;
    }

    public void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }



}