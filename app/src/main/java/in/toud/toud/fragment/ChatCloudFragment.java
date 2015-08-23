package in.toud.toud.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;
import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.adapter.ChatCloudAdapter;
import in.toud.toud.adapter.SimpleStringRecyclerViewAdapter;
import in.toud.toud.chat.CMessage;
import in.toud.toud.chat.ChatCloudM;
import in.toud.toud.events.ChatCloudRecievedEvent;
import in.toud.toud.events.MessageRecievedEvent;
import in.toud.toud.model.ChatCloud;
import in.toud.toud.model.Message;
import in.toud.toud.other.Cheeses;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by rpiyush on 8/12/15.
 */
public class ChatCloudFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String DEBUG_TAG = "ChatCloudFragment";
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    ArrayList<ChatCloudM> chatClouds;
    Activity activity;
    private OnChatCloudSelectedListener mListener;
    private TinyBus mBus;
    ChatCloudAdapter adapter;

    public ChatCloudFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adapter = new ChatCloudAdapter(activity, chatClouds);
        this.activity = activity;
        try {
            mListener = (OnChatCloudSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChatCloudSelectedListener");
        }
    }

    public void onButtonPressed(int position) {
        if (mListener != null) {
            mListener.onChatCloudSelected(position);
        }
    }

    @Subscribe
    public void getMessageEvent(ChatCloudRecievedEvent event) {
        ChatCloudM chatCloudM = event.chatCloud;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBus = TinyBus.from(AppController.getAppContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }
        View view = inflater.inflate(R.layout.fragment_chat_cloud, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        chatClouds = new ArrayList<ChatCloudM>();
        adapter = new ChatCloudAdapter(AppController.getAppContext(), chatClouds);
        gridView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // Send the event to the host activity
        mListener.onChatCloudSelected(position);
    }

    private void reloadChatCloud() {
        chatClouds.clear();
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmResults<ChatCloud> chatCloudRealmResults = realm.where(in.toud.toud.model.ChatCloud.class)
                .findAllSorted("chatCloudTag");
        for (int i = 0; i < chatCloudRealmResults.size(); i++) {
            in.toud.toud.model.ChatCloud chatCloud = chatCloudRealmResults.get(i);
            chatClouds.add(new ChatCloudM(chatCloud.getChatCloudTag()));
        }
        //Log.d(DEBUG_TAG);
        adapter.notifyDataSetChanged();
    }

    public void updateChatCloudView(int position) {
        TextView chatCloudText = (TextView) getActivity().findViewById(R.id.chatCloudText);
        chatCloudText.setText(chatClouds.get(position).getChatCloudTag());
        mCurrentPosition = position;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnChatCloudSelectedListener {
        // TODO: Update argument type and name
        public void onChatCloudSelected(int position);
    }

/*    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity
        mListener.onChatCloudSelected(position);
    }*/

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateChatCloudView(args.getInt(ARG_POSITION));
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateChatCloudView(mCurrentPosition);
        }
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadChatCloud();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

}
