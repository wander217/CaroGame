package com.trinhthinh.caro.adapter;

import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trinhthinh.caro.R;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.AvatarTask;

import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.RankHolder> {
    private final List<User> userRankList;
    private ProgressDialog dialog;

    public RankAdapter(List<User> userRankList) {
        this.userRankList = userRankList;
    }

    @Override
    public RankHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rank_layout,parent,false);
        this.dialog = new ProgressDialog(v.getContext());
        return new RankHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RankAdapter.RankHolder holder, int position) {
        User user = this.userRankList.get(position);
        holder.elo.setText(String.valueOf(user.getInfo().getElo()));
        holder.nickname.setText(user.getInfo().getNickname());
        holder.rank.setText(String.valueOf(position+1));
        AvatarTask avatarTask = new AvatarTask(holder.avatar,user,null);
        avatarTask.execute();
    }

    @Override
    public int getItemCount() {
        return this.userRankList.size();
    }


    public class RankHolder extends RecyclerView.ViewHolder{
        private TextView rank,nickname,elo;
        private ImageView avatar;

        public RankHolder( View itemView) {
            super(itemView);
            this.rank = itemView.findViewById(R.id.rank_user_rank);
            this.nickname= itemView.findViewById(R.id.rank_user_nickname);
            this.elo = itemView.findViewById(R.id.rank_user_elo);
            this.avatar = itemView.findViewById(R.id.rank_user_avatar);
        }
    }
}
