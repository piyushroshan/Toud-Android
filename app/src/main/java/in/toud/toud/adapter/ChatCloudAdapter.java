package in.toud.toud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import in.toud.toud.R;
import in.toud.toud.chat.ChatCloudM;

/**
 * Created by rpiyush on 23/8/15.
 */
public class ChatCloudAdapter extends RecyclerView.Adapter<ChatCloudAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<ChatCloudM> mChatClouds;

    public ChatCloudAdapter(Context context, ArrayList<ChatCloudM> chatClouds) {
        super();
        this.mContext = context;
        this.mChatClouds = chatClouds;
    }
    @Override
    public int getItemCount() {
        return mChatClouds.size();
    }

    public Object getItem(int position) {
        return mChatClouds.get(position);
    }

    @Override
    public ChatCloudAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_cloud_single, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ChatCloudM chatCloud = mChatClouds.get(position);
        holder.chatCloudTag.setText(chatCloud.getChatCloudTag());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.chatCloudTag.getLayoutParams();
        //check the chatCloud position
        holder.chatCloudTag.setText(chatCloud.getChatCloudTag());

        //check if it is a status chatCloudTag then remove background, and change text color.
        if (position % 2 == 0) {
            //holder.chatCloudTag.setBackgroundResource(null);
            holder.chatCloudTag.setTextColor(R.color.blue);
        } else {
            //holder.chatCloudTag.setBackgroundResource(null);
            holder.chatCloudTag.setTextColor(R.color.textColor);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chatCloudTag;

        public ViewHolder(View v) {
            super(v);
            chatCloudTag = (TextView) v.findViewById(R.id.chat_cloud_tag);
        }
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }

}