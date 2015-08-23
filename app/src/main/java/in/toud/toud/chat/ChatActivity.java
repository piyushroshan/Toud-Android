package in.toud.toud.chat;

import java.util.ArrayList;
import java.util.Random;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.model.User;
import in.toud.toud.service.XMMPService;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by rpiyush on 23/8/15.
 */
public class ChatActivity extends ListActivity {

    ArrayList<CMessage> messages;
    CMessageAdapter adapter;
    EditText text;
    static Random rand = new Random();
    static String sender;
    XMMPService service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_messages);
/*        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(AppController.getAppContext());
        realm.beginTransaction();
        realm.commitTransaction();*/
        // Add a person
        Realm realm = Realm.getInstance(AppController.getAppContext());

        User yourJID = new User();
        yourJID.setUsername("roshan");
        yourJID.setPassword("roshan");
        yourJID.setNickName("Roshan Piyush");
        yourJID.setIsAvailable(true);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(yourJID);
        realm.commitTransaction();
        //mRosterHolderListener.createRoster("toud@192.168.1.6");
        service = new XMMPService();
        realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        service.connect(myself);

        text = (EditText) this.findViewById(R.id.text);

        sender = "toud@192.168.1.6";
        this.setTitle(sender);
        messages = new ArrayList<CMessage>();

        messages.add(new CMessage("Hello",sender));
        messages.add(new CMessage("Hi!", "roshan"));
        adapter = new CMessageAdapter(this, messages);
        setListAdapter(adapter);
        addNewMessage(new CMessage("mmm, well, using 9 patches png to show them.", sender));
    }

    public void sendMessage(View v)
    {
        String newMessage = text.getText().toString().trim();
        if(newMessage.length() > 0)
        {
            text.setText("");
            addNewMessage(new CMessage(newMessage, true));
            service.sendMessageToChat(sender,newMessage);
        }
    }

    //@Subscribe
    public void getMessageEvent(MessageRecievedEvent event){
        CMessage message = event.cmessage;
        //Toast.makeText(this, "Event handled", Toast.LENGTH_SHORT).show();
        if (message.isStatusMessage()) {
            if (messages.get(messages.size() - 1).isStatusMessage)//check wether we have already added a status message
            {
                messages.get(messages.size() - 1).setMessage(message.getMessage()); //update the status for that
                adapter.notifyDataSetChanged();
                getListView().setSelection(messages.size() - 1);
            } else {
                addNewMessage(message); //add new message, if there is no existing status message
            }
        } else {

            if (messages.get(messages.size() - 1).isStatusMessage)//check if there is any status message, now remove it.
            {
                messages.remove(messages.size() - 1);
            }

            addNewMessage(message); // add the orignal message from server.
        }
    }




    void addNewMessage(CMessage m)
    {
        messages.add(m);
        adapter.notifyDataSetChanged();
        getListView().setSelection(messages.size()-1);
    }

}