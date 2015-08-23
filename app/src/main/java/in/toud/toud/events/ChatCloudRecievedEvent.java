package in.toud.toud.events;

import in.toud.toud.chat.ChatCloudM;

/**
 * Created by rpiyush on 8/12/15.
 */
public class ChatCloudRecievedEvent {
    public ChatCloudM chatCloud;

    public ChatCloudRecievedEvent(ChatCloudM chatCloud) {
        this.chatCloud = chatCloud;
    }
}
