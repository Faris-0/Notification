package com.yuuna.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdater extends RecyclerView.Adapter<MessageAdater.Holder> {

    private ArrayList<MessageData> messageDataList;
    private Context mContext;

    public MessageAdater(ArrayList<MessageData> messageDataArrayList, Context context) {
        this.messageDataList = messageDataArrayList;
        this.mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        MessageData messageData = messageDataList.get(position);
        holder.tvIDREAD.setText(messageData.getId() + " - " + messageData.getRead());
        holder.tvName.setText(messageData.getName());
        holder.tvMessage.setText(messageData.getMessage());
        holder.tvDatetime.setText(messageData.getDatetime());
    }

    @Override
    public int getItemCount() {
        return messageDataList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvIDREAD, tvName, tvMessage, tvDatetime;

        public Holder(View itemView) {
            super(itemView);
            tvIDREAD = itemView.findViewById(R.id.imIDREAD);
            tvName = itemView.findViewById(R.id.imName);
            tvMessage = itemView.findViewById(R.id.imMessage);
            tvDatetime = itemView.findViewById(R.id.imDatetime);
        }
    }
}
