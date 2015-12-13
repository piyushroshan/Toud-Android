package in.toud.toud.events;

import org.jivesoftware.smackx.chatstates.ChatState;

import in.toud.toud.chat.CMessage;

/**
 * Created by rpiyush on 9/12/15.
 */
public class SendMessageEvent {
    public ChatState chatState;
    public CMessage message;
    public int type;
    public String buddy;

    public SendMessageEvent(ChatState chatState, CMessage message,
                            String buddy, int type) {
        this.chatState = chatState;
        this.message = message;
        this.type = type;
        this.buddy = buddy;
    }

}
