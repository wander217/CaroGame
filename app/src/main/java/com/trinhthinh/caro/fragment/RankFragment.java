package com.trinhthinh.caro.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.ConstantValue;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.adapter.RankAdapter;
import com.trinhthinh.caro.model.Account;
import com.trinhthinh.caro.model.Info;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.AvatarTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankFragment extends Fragment {
    private RecyclerView userList;
    private TextView rank,nickname,elo;
    private ImageView avatar;
    private List<User> userRankList;
    private FirebaseDatabase database;
    private RankAdapter rankAdapter;
    private Account accountSignIn;

    public RankFragment(Account account) {
        this.userRankList = new ArrayList<>();
        this.database = FirebaseDatabase.getInstance(ConstantValue.DATABASE_URL);
        this.accountSignIn = account;
        this.rankAdapter = new RankAdapter(this.userRankList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rank, container, false);
        this.userList = v.findViewById(R.id.rank_user);
        this.rank = v.findViewById(R.id.rank_myRank_rank);
        this.nickname = v.findViewById(R.id.rank_myRank_nickname);
        this.avatar = v.findViewById(R.id.rank_myRank_avatar);
        this.elo = v.findViewById(R.id.rank_myRank_elo);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(v.getContext());
        this.userList.setLayoutManager(linearLayoutManager);
        this.userList.setAdapter(this.rankAdapter);
        loadRank();
        return v;
    }

    private void loadRank(){
        this.database
                .getReference("users").orderByChild("elo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userRankList.clear();
                        long rankCount = snapshot.getChildrenCount();
                        for(DataSnapshot data:snapshot.getChildren()){
                            User user = new User();
                            Account account = new Account();
                            account.setId(data.getKey());
                            user.setAccount(account);
                            Info info = data.getValue(Info.class);
                            user.setInfo(info);
                            userRankList.add(user);
                            if(accountSignIn.getId().equals(data.getKey())){
                                rank.setText(String.valueOf(rankCount));
                                nickname.setText(info.getNickname());
                                elo.setText(String.valueOf(info.getElo()));
                                AvatarTask task = new AvatarTask(avatar,user,null);
                                task.execute();
                            }
                            rankCount--;
                        }
                        Collections.reverse(userRankList);
                        rankAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) { }
                });
    }
}