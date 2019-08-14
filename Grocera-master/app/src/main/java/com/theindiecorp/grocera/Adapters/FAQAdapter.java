package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.theindiecorp.grocera.Data.Text;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Text> dataSet;

    public int setTexts(ArrayList<Text> dataSet)
    {
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView contentTxt;

        public MyViewHolder(View view){
            super(view);
            contentTxt = view.findViewById(R.id.text);
        }
    }

    public FAQAdapter(Context context , ArrayList<Text> dataSet){
        this.dataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int listPosition) {
        Text text = dataSet.get(listPosition);
        holder.contentTxt.setText(text.getContent());

        if(text.getSentBy().equals("user")){
            holder.contentTxt.setBackground(context.getDrawable(R.drawable.sent_box));
            holder.contentTxt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        else{
            holder.contentTxt.setBackground(context.getDrawable(R.drawable.received_box));
            holder.contentTxt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
