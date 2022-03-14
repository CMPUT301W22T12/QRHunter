package com.example.qrhunter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.leaderboardViewAdapter> {

    List<LeaderboardData> list;
    Context context;
    // int i = 1;

    public LeaderboardAdapter(List<LeaderboardData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public leaderboardViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item_list, parent, false);
        return new leaderboardViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull leaderboardViewAdapter holder, int position) {
        LeaderboardData currentItem = list.get(position);
        holder.username.setText(currentItem.getName());
        holder.score.setText(String.valueOf(currentItem.getScore()));
        holder.rank.setText(String.valueOf(list.size() - position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class leaderboardViewAdapter extends RecyclerView.ViewHolder {
        TextView username, score, rank;
        public leaderboardViewAdapter(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.leaderboard_username);
            score = itemView.findViewById(R.id.leaderboard_score);
            rank = itemView.findViewById(R.id.leaderboard_rank);
        }
    }
}
