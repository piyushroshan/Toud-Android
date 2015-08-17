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
    private String Message;
    private long TimeStamp;
    private boolean isRead;
    private boolean isSent;
    private String from;
    private String to;
    private long time;

    public long getId() {
        return this.getId();
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public boolean isRead() {
        return isRead;
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

    public boolean isSent() {
        return isRead;
    }

    public void setIsSent(boolean isReaded) {
        this.isRead = isReaded;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }


}