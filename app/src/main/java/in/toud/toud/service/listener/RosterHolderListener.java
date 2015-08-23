package in.toud.toud.service.listener;

/**
 * Created by rpiyush on 15/8/15.
 */

import android.content.ContentValues;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import in.toud.toud.AppController;
import in.toud.toud.model.JID;
import in.toud.toud.model.User;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by rpiyush on 15/8/15.
 */
public class RosterHolderListener implements RosterListener {


    public final static String DEBUG_TAG = RosterHolderListener.class.getSimpleName();
    public final static int INSERT_JID = 0;
    public final static int UPDATE_JID_STATUS = 1;
    private final Roster mRoster;
    private ArrayList<RosterEntry> mRosterEntries;
    public DBAsyncTask mDBAsyncTask;


    public RosterHolderListener(XMPPConnection xmppConnection) {
        mRoster = Roster.getInstanceFor(xmppConnection);
        mRoster.addRosterListener(this);
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        mDBAsyncTask = new DBAsyncTask();
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        Log.d(DEBUG_TAG, "entries Added: " + Arrays.toString(addresses.toArray()));
        mDBAsyncTask = new DBAsyncTask();
        mDBAsyncTask.execute(INSERT_JID);
    }


    @Override
    public void entriesUpdated(Collection<String> addresses) {
        Log.d(DEBUG_TAG, "entries Updated: " + Arrays.toString(addresses.toArray()));
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        Log.d(DEBUG_TAG, "entries Deleted: " + Arrays.toString(addresses.toArray()));
    }

    @Override
    public void presenceChanged(Presence presence) {
        Log.d(DEBUG_TAG, "Presence changed: " + presence.getFrom() + " " + presence.getType()
                + " " + presence.getStatus());
        mDBAsyncTask = new DBAsyncTask();
        mDBAsyncTask.execute(UPDATE_JID_STATUS, presence);
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

    }

    public void stopDBTask() {
        if (!mDBAsyncTask.isCancelled())
            mDBAsyncTask.cancel(true);
    }

    /**
     * Insert new JID to db or update if it exists after that update JID status
     */
    private void insertUserJIDS() {
        ContentValues contentValues;

        mRosterEntries = new ArrayList<>(mRoster.getEntries());
        Realm realm = Realm.getInstance(AppController.getAppContext());
        for (RosterEntry entry : mRosterEntries) {
            JID jid = new JID();
            Log.d("GET_FROM",entry.getName()+entry.getUser());
            jid.setNickName(entry.getName());
            jid.setJID(entry.getUser());
            Presence presence = mRoster.getPresence(entry.getUser());
            jid.setIsAvailable(presence.isAvailable());
            jid.setPresence(presence.getStatus());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(jid);
            realm.commitTransaction();
        }
    }

    /**
     * Update Status (available/unavailable) of JID
     *
     * @param presence
     */
    private void updateStatus(Presence presence) {
        Log.d("GET_FROM",presence.getFrom());
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery<JID> query = realm.where(JID.class).equalTo("JID", presence.getFrom().split("/")[0]);
        JID user1 = query.findFirst();
        if (user1 == null) {
            user1 = new JID();
            user1.setNickName(presence.getFrom());
            user1.setJID(presence.getFrom().split("/")[0]);
            user1.setIsAvailable(presence.isAvailable());
            user1.setPresence(presence.getStatus());
        }
        realm.beginTransaction();
        user1.setPresence(presence.getType().name());
        realm.commitTransaction();
    }

    public void createRoster(String user) {
        try {
            mRoster.createEntry(user, user, new String[]{"No group"});
            Realm realm = Realm.getInstance(AppController.getAppContext());
            RealmQuery<JID> query = realm.where(JID.class).equalTo("JID", user);
            JID user1 = query.findFirst();
            realm.beginTransaction();
            user1.setGroup("No group");
            realm.commitTransaction();
        } catch (SmackException.NotLoggedInException e) {
            Log.e(DEBUG_TAG, "EXCEPTION", e);
        } catch (SmackException.NoResponseException e) {
            Log.e(DEBUG_TAG, "EXCEPTION", e);
        } catch (XMPPException.XMPPErrorException e) {
            Log.e(DEBUG_TAG, "EXCEPTION", e);
        } catch (SmackException.NotConnectedException e) {
            Log.e(DEBUG_TAG, "EXCEPTION", e);
        }
    }

    private class DBAsyncTask extends AsyncTask<Object, Integer, String> {
        /**
         * @param objects
         * @return
         */
        protected String doInBackground(Object... objects) {
            Log.d(DEBUG_TAG, " doInBackground with code" + objects[0]);
            String resultMessage = "something goes wrong!";
            switch ((int) objects[0]) {
                case INSERT_JID:
                    insertUserJIDS();
                    resultMessage = " jid inserted";
                    break;
                case UPDATE_JID_STATUS:
                    if (!(objects[1] instanceof Presence)) {
                        throw new IllegalArgumentException("With that code you hate to pass Presence in object with index 1!");
                    }
                    Presence presence = (Presence) objects[1];
                    updateStatus(presence);
                    break;
            }

            return resultMessage;
        }


        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, " onPostExecute " + result);
        }
    }
}