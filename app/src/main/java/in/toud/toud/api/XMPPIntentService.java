package in.toud.toud.api;

import android.app.IntentService;
import android.content.Intent;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by rpiyush on 15/8/15.
 */

public class XMPPIntentService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public XMPPIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}