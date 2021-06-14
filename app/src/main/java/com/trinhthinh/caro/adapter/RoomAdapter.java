package com.trinhthinh.caro.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.ConstantValue;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.WaitingRoomActivity;
import com.trinhthinh.caro.model.Room;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.RoomTask;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomHolder> {
    private final List<Room> roomList;
    private final Activity activity;
    private FirebaseDatabase database;
    private final User user;

    public RoomAdapter(List<Room> roomList, Activity activity, User user) {
        this.roomList = roomList;
        this.activity = activity;
        this.user = user;
        this.database = FirebaseDatabase
                .getInstance(ConstantValue.DATABASE_URL);
    }

    @Override
    public RoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_layout,parent,false);
        return new RoomHolder(v) ;
    }

    @Override
    public void onBindViewHolder(@NonNull  RoomHolder holder, int position) {
        Room r = roomList.get(position);
        holder.nickname.setText(r.getUser1().getInfo().getNickname());
        holder.elo.setText("Elo: "+r.getUser1().getInfo().getElo());
        holder.id.setText(r.getName());
        holder.money.setText(String.valueOf(r.getMoney()));
        holder.rule.setText(r.isBlock2way()?"Chặn hai đầu":"Không chặn hai đầu");
        holder.player.setText(r.getUser2() instanceof User ? "2/2":"1/2");
        RoomTask roomTask = new RoomTask(holder.avatar,r);
        roomTask.execute();
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoom(r);
            }
        });
    }

    private void showRoom(Room r){
        this.database
                .getReference("rooms").child("playing_room").child(r.getId())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Room room = currentData.getValue(Room.class);
                        if(!(room instanceof Room)){
                            Toast.makeText(activity.getApplicationContext()
                                    ,"Phòng không tồn tại",Toast.LENGTH_LONG).show();
                            Transaction.success(currentData);
                        }
                        if(room.getUser2() instanceof User){
                            Toast.makeText(activity.getApplicationContext()
                                    ,"Phòng đã đủ người",Toast.LENGTH_LONG).show();
                        }else{
                            room.setUser2(user);
                            currentData.setValue(room);
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable  DatabaseError error, boolean committed
                            , @Nullable  DataSnapshot currentData) {
                        Intent intent = new Intent(activity.getApplicationContext(),
                                WaitingRoomActivity.class);
                        intent.putExtra("room_id",r.getId());
                        intent.putExtra("user",user);
                        activity.startActivity(intent);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.roomList.size();
    }

    public class RoomHolder extends RecyclerView.ViewHolder{
        private ImageView avatar;
        private TextView nickname,elo,id,money,rule,player;
        public RoomHolder(@NonNull View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.room_user_avatar);
            this.nickname = itemView.findViewById(R.id.room_user_nickname);
            this.elo = itemView.findViewById(R.id.room_user_elo);
            this.id = itemView.findViewById(R.id.room_id);
            this.money = itemView.findViewById(R.id.room_fee);
            this.rule = itemView.findViewById(R.id.room_rule);
            this.player = itemView.findViewById(R.id.room_player);
        }
    }
}
