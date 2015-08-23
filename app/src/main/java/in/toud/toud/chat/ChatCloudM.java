package in.toud.toud.chat;

import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.JID;

/**
 * Created by rpiyush on 8/12/15.
 */
public class ChatCloudM {

    String chatCloudTag;

    public ChatCloudM(String chatCloudTag) {
        super();
        this.chatCloudTag = chatCloudTag;
    }

    public String getChatCloudTag() {
        return this.chatCloudTag;
    }

    public void setChatCloudTag(String chatCloudTag) {
        this.chatCloudTag = chatCloudTag;
    }
}
