package in.toud.toud.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import org.jivesoftware.smackx.chatstates.ChatState;

import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;
import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.adapter.CMessageAdapter;
import in.toud.toud.chat.CMessage;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.Message;
import in.toud.toud.service.XMMPService;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by rpiyush on 23/8/15.
 */
public class ChatFragment extends ListFragment {

    ArrayList<CMessage> messages;
    CMessageAdapter adapter;
    XMMPService service;
    String chatCloud;
    String buddy;
    private TinyBus mBus;
    private OnChatMessageInteractionListener mListener;
    Activity activity;
    private static final String DEBUG_TAG = "ChatFragment";
    public final static String ARG_POSITION = "position";
    Boolean mBound;
    EditText chat_text;
    int position;

    public ChatFragment() {
        super();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            XMMPService.LocalBinder binder = (XMMPService.LocalBinder) service;
            service = (IBinder) binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
            mBound = false;
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        adapter = new CMessageAdapter(activity, messages);
        try {
            mListener = (OnChatMessageInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void onButtonPressed(int position) {
        if (mListener != null) {
            mListener.onChatMessageInteraction(position);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_messages, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBus = TinyBus.from(AppController.getAppContext());
        //buddy = "toud@52.90.175.123";
        messages = new ArrayList<CMessage>();
        adapter = new CMessageAdapter(AppController.getAppContext(), messages);
        setListAdapter(adapter);
        //addNewMessage(new CMessage("mmm, well, using 9 patches png to show them.", buddy));
        //this.reloadChat();
        chat_text = (EditText) getActivity().findViewById(R.id.chat_text);
        chat_text.addTextChangedListener(mTextEditorWatcher);
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 3) {
                sendChatState(ChatState.composing);
            }
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0)
                sendChatState(ChatState.active);
            else
                sendChatState(ChatState.paused);
        }
    };

    @Subscribe
    public void getMessageEvent(MessageRecievedEvent event) {
        CMessage message = event.cmessage;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("chatCloudTag").get(position);
        if (message.isStatusMessage() || message.getChatCloudTag().equals(chatCloud.getChatCloudTag())) {
            addNewMessage(message); // add the orignal message from server.
        }
    }

    public void sendMessage() {
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("chatCloudTag").get(position);
        String buddy = chatCloud.getSupport();
        String newMessage = chat_text.getText().toString().trim();
        if (newMessage.length() > 0) {
            chat_text.setText("");
            CMessage cMessage = new CMessage(chatCloud.getChatCloudTag(), newMessage, true, false);
            TinyBus.from(AppController.getAppContext()).post(new MessageRecievedEvent(cMessage));
            service.sendMessageToChat(buddy, chatCloud.getChatCloudTag() + '#' + newMessage);
        }
    }

    public void sendChatState(ChatState chatState) {
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("chatCloudTag").get(position);
        String buddy = chatCloud.getSupport();
        service.sendStateToChat(buddy, chatState);
    }

    void addNewMessage(CMessage message) {
        //Toast.makeText(this, "Event handled", Toast.LENGTH_SHORT).show();
        if (messages.size() > 0) {
            if (message.isStatusMessage()) {
                if (messages.get(messages.size() - 1).isStatusMessage())//check wether we have already added a status message
                {
                    messages.get(messages.size() - 1).setMessage(message.getMessage()); //update the status for that
                    adapter.notifyDataSetChanged();
                    getListView().setSelection(messages.size() - 1);
                } else {
                    messages.add(message); //add new message, if there is no existing status message
                    adapter.notifyDataSetChanged();
                    getListView().setSelection(messages.size() - 1);
                }
            } else {

                if (messages.get(messages.size() - 1).isStatusMessage())//check if there is any status message, now remove it.
                {
                    messages.remove(messages.size() - 1);
                }

                messages.add(message); // add the orignal message from server.
                adapter.notifyDataSetChanged();
                getListView().setSelection(messages.size() - 1);
            }
        } else {
            messages.add(message); //add new message, if there is no existing status message
            adapter.notifyDataSetChanged();
            getListView().setSelection(messages.size() - 1);
        }
        sendChatState(ChatState.active);
    }

    private void reloadChat() {
        messages.clear();
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("chatCloudTag").get(position);
        String buddy = chatCloud.getSupport();
        RealmResults<Message> chatRealmResults = realm.where(in.toud.toud.model.Message.class)
                .beginsWith("to", buddy).or().equalTo("from", buddy)
                .equalTo("chatCloudTag", chatCloud.getChatCloudTag()).findAllSorted("time");
        for (int i = 0; i < chatRealmResults.size(); i++) {
            in.toud.toud.model.Message chatMessage = chatRealmResults.get(i);
            messages.add(new CMessage(chatCloud.getChatCloudTag(), chatMessage.getMessage(), chatMessage.getFrom()));
        }
        Log.d(DEBUG_TAG, buddy + " weaponmike-----------------");
        adapter.notifyDataSetChanged();
        getListView().setSelection(messages.size() - 1);
        sendChatState(ChatState.active);
        service.sendMessagesPending();
    }


    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        Intent intent = new Intent(getActivity(), XMMPService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        getActivity().unbindService(mConnection);
        mBound = false;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.reloadChat();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface OnChatMessageInteractionListener {
        // TODO: Update argument type and name
        public void onChatMessageInteraction(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity
        mListener.onChatMessageInteraction(position);
    }

}