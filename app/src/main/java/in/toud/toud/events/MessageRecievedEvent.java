package in.toud.toud.events;

import in.toud.toud.chat.CMessage;

/**
 * Created by rpiyush on 23/8/15.
 */
public class MessageRecievedEvent {
    public CMessage cmessage;

    public MessageRecievedEvent(CMessage cmessage) {
        this.cmessage=cmessage;
    }
}
