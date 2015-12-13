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
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import org.jivesoftware.smackx.chatstates.ChatState;

import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;
import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.adapter.CMessageAdapter;
import in.toud.toud.chat.CMessage;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.events.SendMessageEvent;
import in.toud.toud.listener.RecycleItemClickListener;
import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.Message;
import in.toud.toud.service.XMMPService;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by rpiyush on 23/8/15.
 */
public class ChatFragment extends Fragment {

    ArrayList<CMessage> messages;
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter<CMessageAdapter.ViewHolder> adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String chatCloud;
    String buddy;
    private TinyBus mBus;
    private OnChatMessageInteractionListener mListener;
    Activity activity;
    private static final String DEBUG_TAG = "ChatFragment";
    public final static String ARG_POSITION = "position";
    EditText chat_text;
    Button chat_button;
    int position;

    public ChatFragment() {
        super();
    }

    public EditText getChat_text() {
        return chat_text;
    }

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
        View view = inflater.inflate(R.layout.fragment_chat_messages, container, false);
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycle_chat_view);
        mRecycleView.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.addOnItemTouchListener(
                new RecycleItemClickListener(AppController.getAppContext(), new RecycleItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                        mListener.onChatMessageInteraction(position);
                    }
                })
        );
        Bundle bundle = this.getArguments();
        position = bundle.getInt(ARG_POSITION);
        chat_text = (EditText) view.findViewById(R.id.chat_text);
        chat_text.addTextChangedListener(mTextEditorWatcher);
        chat_button = (Button) view.findViewById(R.id.send_button);
        chat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(in.toud.toud.model.ChatCloud.class)
                .findAllSorted("createdOn").get(position);
        TextView chatCloudText = (TextView) view.findViewById(R.id.chatCloudText);
        chatCloudText.setText(chatCloud.getChatCloudTag());
        Log.d(DEBUG_TAG, buddy + " View Created Hulalalala-----------------");
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBus = TinyBus.from(AppController.getAppContext());
        //buddy = "toud@52.90.175.123";
        messages = new ArrayList<CMessage>();
        adapter = new CMessageAdapter(AppController.getAppContext(), messages);
        //addNewMessage(new CMessage("mmm, well, using 9 patches png to show them.", buddy));
        //this.reloadChat();
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

    @Subscribe(mode = Subscribe.Mode.Background)
    public void getMessageEvent(MessageRecievedEvent event) {
        CMessage message = event.cmessage;
        Realm realm = Realm.getInstance(AppController.getAppContext());
        realm.refresh();
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("createdOn").get(position);
        if (message.isStatusMessage() || message.getChatCloudTag().equals(chatCloud.getChatCloudTag())) {
            addNewMessage(message); // add the orignal message from server.
        }
        Log.d(getActivity().getClass().getSimpleName(), message.getMessage() + "  Hulalalalala");
    }

    public void sendChatState(ChatState chatState) {
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("createdOn").get(position);
        String buddy = chatCloud.getSupport();
        TinyBus.from(AppController.getAppContext()).post(new SendMessageEvent(chatState, null, buddy, 1));
    }

    void addNewMessage(final CMessage message) {
        //Toast.makeText(this, "Event handled", Toast.LENGTH_SHORT).show();
        if (messages.size() > 0) {
            if (message.isStatusMessage()) {
                if (messages.get(messages.size() - 1).isStatusMessage())//check wether we have already added a status message
                {
                    messages.get(messages.size() - 1).setMessage(message.getMessage()); //update the status for that
                } else {
                    messages.add(message); //add new message, if there is no existing status message
                }
            } else {

                if (messages.get(messages.size() - 1).isStatusMessage())//check if there is any status message, now remove it.
                {
                    messages.remove(messages.size() - 1);
                }

                messages.add(message); // add the orignal message from server.
            }
        } else {
            messages.add(message); //add new message, if there is no existing status message
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (!message.isStatusMessage()) {
                    scrollViewToPosition(adapter.getItemCount());
                }
            }
        });
        sendChatState(ChatState.active);
    }

    private void reloadChat() {
        messages.clear();
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("createdOn").get(position);
        Log.d(DEBUG_TAG, Integer.toString(position) + "  " + chatCloud.getChatCloudTag() + "ChatPosition-------------Position-----------hulalala");
        String buddy = chatCloud.getSupport();
        RealmResults<Message> chatRealmResults = realm.where(in.toud.toud.model.Message.class)
                .equalTo("chatCloudTag", chatCloud.getChatCloudTag()).findAllSorted("time");
        for (int i = 0; i < chatRealmResults.size(); i++) {
            in.toud.toud.model.Message chatMessage = chatRealmResults.get(i);
            messages.add(new CMessage(chatCloud.getChatCloudTag(), chatMessage.getMessage(), chatMessage.getFrom()));
        }
        Log.d(DEBUG_TAG, buddy + " weaponmike-----------------");
        adapter.notifyDataSetChanged();
        sendChatState(ChatState.active);
        TinyBus.from(AppController.getAppContext()).post(new SendMessageEvent(null, null, null, 2));
        mRecycleView.post(new Runnable() {

            @Override
            public void run() {
                mRecycleView.scrollToPosition(adapter.getItemCount());
            }
        });
    }

    public void sendMessage(View v) {
        Realm realm = Realm.getInstance(AppController.getAppContext());
        ChatCloud chatCloud = realm.where(ChatCloud.class).findAllSorted("createdOn").get(position);
        String buddy = chatCloud.getSupport();
        String newMessage = chat_text.getText().toString().trim();
        if (newMessage.length() > 0) {
            chat_text.setText("");
            CMessage cMessage = new CMessage(chatCloud.getChatCloudTag(), newMessage, true, false);
            TinyBus.from(AppController.getAppContext()).post(new MessageRecievedEvent(cMessage));
            TinyBus.from(AppController.getAppContext()).post(new SendMessageEvent(null, cMessage, buddy, 0));
        }
    }

    private void scrollViewToPosition(final int position) {
        mRecycleView.post(new Runnable() {
            @Override
            public void run() {
                //call smooth scroll
                mRecycleView.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        this.reloadChat();
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface OnChatMessageInteractionListener {
        // TODO: Update argument type and name
        public void onChatMessageInteraction(int position);
    }

}