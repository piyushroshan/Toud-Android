package in.toud.toud.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import in.toud.toud.R;
import in.toud.toud.chat.CMessage;

/**
 * Created by rpiyush on 23/8/15.
 */
public class CMessageAdapter extends RecyclerView.Adapter<CMessageAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<CMessage> mMessages;

    public CMessageAdapter(Context context, ArrayList<CMessage> messages) {
        super();
        this.mContext = context;
        this.mMessages = messages;
    }
    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public CMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_single_message, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CMessage message = mMessages.get(position);
        holder.message.setText(message.getMessage());
        LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
        //check if it is a status message then remove background, and change text color.
        if(message.isStatusMessage())
        {
            holder.message.setBackgroundDrawable(null);
            lp.gravity = Gravity.LEFT;
            holder.message.setTextColor(R.color.textFieldColor);
        }
        else
        {
            //Check whether message is mine to show green background and align to right
            if(message.isMine())
            {
                holder.message.setBackgroundResource(R.drawable.bubble_me);
                lp.gravity = Gravity.RIGHT;
            }
            //If not mine then it is from sender to show orange background and align to left
            else
            {
                holder.message.setBackgroundResource(R.drawable.bubble_you);
                lp.gravity = Gravity.LEFT;
            }
            holder.message.setLayoutParams(lp);
            holder.message.setTextColor(R.color.textColor);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView message;

        public ViewHolder(View v) {
            super(v);
            message = (TextView) v.findViewById(R.id.message_text);
        }
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }

}