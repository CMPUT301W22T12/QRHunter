package com.example.qrhunter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private ArrayList<LeaderboardHolder> info = new ArrayList<>();
    private ArrayList<LeaderboardHolder> infoAll;
    private OnItemListener ItemListener;

    public LeaderboardAdapter(ArrayList<LeaderboardHolder> info, Context context, OnItemListener onItemListener) {
        this.info = info;
        this.context = context;
        this.ItemListener = onItemListener;
        this.infoAll = new ArrayList<>(info);
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboardreceycleview,
                parent, false);
        return new ViewHolder(view, ItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        holder.username.setText(info.get(position).getUsername());
        holder.score.setText(info.get(position).getScore());
        holder.rank.setText(info.get(position).getUserRank()+"");
    }

    @Override
    public int getItemCount() {
        return info.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            ArrayList<LeaderboardHolder> filterList = new ArrayList<>();
            if(charSequence.toString().isEmpty()){
                filterList.addAll(infoAll);
            }else{
                for(LeaderboardHolder holder: infoAll){
                    if(holder.getUsername().contains(charSequence.toString())){
                        filterList.add(holder);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            info.clear();
            info.addAll((Collection<? extends LeaderboardHolder>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView username;
        TextView score;
        TextView rank;
        OnItemListener onItemListener;
        public ViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            score = itemView.findViewById(R.id.leaderboardScore);
            rank = itemView.findViewById(R.id.leaderboardRanks);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemListener.OnItemClick(getAdapterPosition());

        }
    }
    public interface OnItemListener{
        int getResourceId();

        int SelectedItemId();

        void OnItemClick(int position);
    }

}
