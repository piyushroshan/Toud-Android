package in.toud.toud.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by rpiyush on 26/7/15.
 */

public class XMPPReamAdapter extends RealmBaseAdapter {


    public XMPPReamAdapter(Context context, RealmResults realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}