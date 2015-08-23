package in.toud.toud.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by rpiyush on 15/8/15.
 */
public class User extends RealmObject {
    public static final String TAG = User.class.getSimpleName();

    @PrimaryKey
    private String username;
    private String password;
    private String nickName;
    private String status;
    private boolean isAvailable;
    @Ignore
    private final String hostUrl = "52.90.175.123";


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public boolean getIsAvailable() {
        return this.isAvailable;
    }

    public void setIsAvailable(boolean available) {
        this.isAvailable = available;
    }
}