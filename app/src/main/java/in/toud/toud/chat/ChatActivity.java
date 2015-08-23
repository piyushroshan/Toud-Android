package in.toud.toud.chat;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.Random;

import butterknife.ButterKnife;
import de.halfbit.tinybus.TinyBus;
import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.fragment.ChatCloudFragment;
import in.toud.toud.fragment.ChatFragment;
import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.User;
import in.toud.toud.service.XMMPService;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by rpiyush on 8/12/15.
 */
public class ChatActivity extends FragmentActivity
        implements ChatCloudFragment.OnChatCloudSelectedListener, ChatFragment.OnChatMessageInteractionListener {

    XMMPService service;
    private TinyBus mBus;
    static Random rand = new Random();
    ChatFragment chatFragment;
    ChatCloudFragment chatCloudFragment;
    EditText chat_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chat_text = (EditText) this.findViewById(R.id.chat_text);
        /*FragmentManager fm = getSupportFragmentManager();
        chatFragment = (ChatFragment) fm.findFragmentById(R.id.fragment_chat_messages_container);
        chatCloudFragment = (ChatCloudFragment) fm.findFragmentById(R.id.fragment_chat_cloud_container);
        chatCloudFragment.setArguments(getIntent().getExtras());*/
        Realm realm = Realm.getInstance(AppController.getAppContext());
        User yourJID = new User();
        yourJID.setUsername("roshan@52.90.175.123");
        yourJID.setPassword("roshan");
        yourJID.setNickName("Roshan Piyush");
        yourJID.setIsAvailable(true);
        ChatCloud chatCloud = new ChatCloud();
        chatCloud.setChatCloudTag("SellMe");
        chatCloud.setLastRecieved(System.currentTimeMillis());
        chatCloud.setLastSent(System.currentTimeMillis());
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(yourJID);
        realm.copyToRealmOrUpdate(chatCloud);
        realm.commitTransaction();
        //mRosterHolderListener.createRoster("toud@52.90.175.123");
        service = new XMMPService();
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        service.connect(myself);
    }

    @Override
    public void onStart() {
        super.onStart();
        //mBus.register(this);
    }

    @Override
    public void onStop() {
        //mBus.unregister(this);
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

    @Override
    public void onChatCloudSelected(int position) {
        // The user selected the ChatCloud

        chatCloudFragment = (ChatCloudFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_chat_cloud_container);

        if (chatCloudFragment != null) {
            // If frag is available, we're in two-pane layout...
            chatCloudFragment.updateChatCloudView(position);
            ChatFragment newFragment = new ChatFragment();
            Bundle args = new Bundle();
            args.putInt(ChatFragment.ARG_POSITION, position);
            newFragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_chat_messages_container, newFragment);
            //transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected chatcloud
            ChatFragment newFragment = new ChatFragment();
            Bundle args = new Bundle();
            args.putInt(ChatFragment.ARG_POSITION, position);
            newFragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_chat_cloud_container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        }
    }

    @Override
    public void onChatMessageInteraction(int position) {
        // The user selected the ChatCloud
    }
}