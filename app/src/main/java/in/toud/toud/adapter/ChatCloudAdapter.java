package in.toud.toud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.toud.toud.R;
import in.toud.toud.chat.ChatCloudM;

/**
 * Created by rpiyush on 8/12/15.
 */
public class ChatCloudAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ChatCloudM> chatClouds;


    public ChatCloudAdapter(Context context, ArrayList<ChatCloudM> chatClouds) {
        super();
        this.mContext = context;
        this.chatClouds = chatClouds;
    }

    @Override
    public int getCount() {
        return chatClouds.size();
    }

    @Override
    public Object getItem(int position) {
        return chatClouds.get(position);
    }

    @Override
    @SuppressWarnings("ResourceAsColor")
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatCloudM chatCloud = (ChatCloudM) this.getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_cloud_single, parent, false);
            holder.chatCloudTag = (TextView) convertView.findViewById(R.id.chat_cloud_tag);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.chatCloudTag.setText(chatCloud.getChatCloudTag());

        //check if it is a status chatCloudTag then remove background, and change text color.
        if (position % 2 == 0) {
            //holder.chatCloudTag.setBackgroundResource(null);
        } else {
            //holder.chatCloudTag.setBackgroundResource(null);
            holder.chatCloudTag.setTextColor(R.color.textColor);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView chatCloudTag;
    }

    @Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }
}
