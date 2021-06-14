package com.trinhthinh.caro.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.ConstantValue;
import com.trinhthinh.caro.ListRoomActivity;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.WaitingRoomActivity;
import com.trinhthinh.caro.model.Account;
import com.trinhthinh.caro.model.Info;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.AvatarTask;

public class MenuFragment extends Fragment {
    private Button room,exit;
    private ProgressDialog dialog;

    private ImageView avatar;
    private TextView nickname,elo,money;

    private final User user;
    private FirebaseDatabase database;

    public MenuFragment(Account account) {
        this.user = new User();
        this.user.setAccount(account);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.database = FirebaseDatabase.getInstance(ConstantValue.DATABASE_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        this.room = v.findViewById(R.id.createRoomButton);
        this.exit= v.findViewById(R.id.exitButton);

        this.dialog= new ProgressDialog(this.getContext());
        this.avatar = v.findViewById(R.id.home_avatar);
        this.nickname = v.findViewById(R.id.home_nickname);
        this.elo = v.findViewById(R.id.home_elo);
        this.money =v.findViewById(R.id.home_money);
        this.dialog.setMessage("Đang tải");
        this.dialog.show();
        this.loadInfo();

        this.exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finishAffinity();
                System.exit(0);
            }
        });

        this.room = v.findViewById(R.id.createRoomButton);
        this.room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ListRoomActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

        return v;
    }

    private void loadInfo() {
        this.database
                .getReference().child("users").child(this.user.getAccount().getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        Info info = snapshot.getValue(Info.class);
                        nickname.setText(info.getNickname());
                        money.setText(String.valueOf(info.getMoney()));
                        elo.setText("Elo: "+info.getElo());
                        user.setInfo(info);
                        AvatarTask avatarTask = new AvatarTask(avatar,user, dialog);
                        avatarTask.execute();
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) { }
                });
    }
}