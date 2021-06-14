package com.trinhthinh.caro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.model.Account;
import com.trinhthinh.caro.model.Room;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.AvatarTask;

public class WaitingRoomActivity extends AppCompatActivity {
    private ImageView back,avatar1,avatar2;
    private TextView name1,name2,elo1,elo2;
    private TextView rule,id,money,count;
    private TextView status;
    private Button ready;
    private Room room;
    private User user;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watting_room);
        init();
    }

    private void init(){
        this.room = new Room();
        Intent roomIntent = getIntent();
        if(roomIntent instanceof Intent){
            String roomId = roomIntent.getStringExtra("room_id");
            this.user = (User) roomIntent.getSerializableExtra("user");
            if(!(roomId instanceof String)||!(this.user instanceof User)){
                Intent intent = new Intent(WaitingRoomActivity.this,LoginActivity.class);
                startActivity(intent);
            }
            this.room.setId(roomId);
        }
        this.avatar1 =  findViewById(R.id.waiting_room_player1_avatar);
        this.name1 = findViewById(R.id.waiting_room_player1_nickname);
        this.elo1 = findViewById(R.id.waiting_room_player1_elo);

        this.avatar2 =  findViewById(R.id.waiting_room_player2_avatar);
        this.name2 = findViewById(R.id.waiting_room_player2_nickname);
        this.elo2 = findViewById(R.id.waiting_room_player2_elo);

        this.id = findViewById(R.id.waiting_room_id);
        this.rule = findViewById(R.id.waiting_room_rule);
        this.money = findViewById(R.id.waiting_room_fee);
        this.count = findViewById(R.id.waiting_room_player_count);

        this.status = findViewById(R.id.waiting_room_status_text);
        this.back =  findViewById(R.id.waiting_room_back);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToListRoom();
            }
        });
        this.ready = findViewById(R.id.waiting_room_ready);
        this.ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ready();
            }
        });
        this.database = FirebaseDatabase
                .getInstance(ConstantValue.DATABASE_URL);
        loadRoom();
    }

    private void loadRoom() {
        this.database
                .getReference("rooms")
                .child("playing_room").child(this.room.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        checkData(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) { }
                });
    }

    private void checkData(DataSnapshot snapshot){
        Room room = snapshot.getValue(Room.class);
        if(!(room instanceof Room)) return;
        this.room = room;
        this.room.setId(snapshot.getKey());

        int countUser = 0;
        if(this.room.getUser1() instanceof User){
            if(this.room.getUser1().getAccount().getId().equals(user.getAccount().getId())){
                setUser1(this.room.getUser1());
                status.setText("Đang chờ đối thủ...");
            }else {
                setUser2(this.room.getUser1());
            }
            countUser++;
        }

        if(this.room.getUser2() instanceof User){
            if(this.room.getUser2().getAccount().getId().equals(user.getAccount().getId())){
                setUser1(this.room.getUser2());
                status.setText("Hãy nhấn sẵn sàng...");
            }else{
                setUser2(this.room.getUser2());
            }
            countUser++;
        }else{
            this.name2.setText("Đối thủ");
            this.elo2.setText("1000");
            this.avatar2.setImageResource(R.drawable.ic_user);
        }

        rule.setText(this.room.isBlock2way()?"Chặn 2 đầu":"5 con thắng");
        id.setText(this.room.getName());
        money.setText(String.valueOf(this.room.getMoney()));
        count.setText(countUser+"/2");

        if(this.room.isUser2Ready()&&this.room.isUser1Ready()){
            this.startMatch();
        }
    }

    private void setUser1(User user1){
        AvatarTask roomTask = new AvatarTask(this.avatar1,user1,null);
        roomTask.execute();
        this.name1.setText(user1.getInfo().getNickname());
        this.elo1.setText(String.valueOf(user1.getInfo().getElo()));
    }

    private void setUser2(User user2){
        AvatarTask roomTask = new AvatarTask(this.avatar2,user2,null);
        roomTask.execute();
        this.name2.setText(user2.getInfo().getNickname());
        this.elo2.setText(String.valueOf(user2.getInfo().getElo()));
    }

    private  void backToListRoom(){
        this.database
                .getReference("rooms")
                .child("playing_room").child(this.room.getId())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(@NonNull  MutableData currentData) {
                        Room room = currentData.getValue(Room.class);
                        if(!(room instanceof Room)){
                            return Transaction.success(currentData);
                        }

                        if(room.getUser1() instanceof User){
                            if(room.getUser1().getAccount()
                                    .getId().equals(user.getAccount().getId())){
                                room.setUser1(null);
                            }
                        }

                        if(room.getUser2() instanceof User){
                            if(room.getUser2().getAccount()
                                    .getId().equals(user.getAccount().getId())){
                                room.setUser2(null);
                            }
                        }

                        if(room.getUser1() == null){
                            if(room.getUser2()!=null){
                                room.setUser1(room.getUser2());
                                room.setUser2(null);
                            }else{
                                room.setDelete(true);
                            }
                        }
                        currentData.setValue(room);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable  DatabaseError error, boolean committed,
                                           DataSnapshot currentData) {
                        Intent  intent = new Intent(WaitingRoomActivity.this
                                ,ListRoomActivity.class);
                        intent.putExtra("user",user);
                        startActivity(intent);
                    }
                });
    }

    private void ready(){
        if(user.getAccount().getId().equals(this
                .room.getUser1().getAccount().getId())){
            if(this.room.isUser2Ready()){
                this.room.setUser1Ready(true);
                this.database.getReference("rooms").child("playing_room")
                        .child(this.room.getId()).setValue(this.room)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                status.setText("Chờ người chơi 1 bắt đầu");
                            }
                        });
            }else {
                Toast.makeText(this.getApplicationContext()
                        ,"Người chơi 2 chưa sẵn sàng!",Toast.LENGTH_LONG).show();
            }
        }else{
            this.room.setUser2Ready(true);
            this.database.getReference("rooms").child("playing_room")
                    .child(this.room.getId()).setValue(this.room)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            status.setText("Chờ người chơi 1 bắt đầu");
                        }
                    });
        }
    }

    private void startMatch(){
        Intent intent = new Intent(WaitingRoomActivity.this,MatchRoomActivity.class);
        intent.putExtra("room_id",this.room.getId());
        intent.putExtra("user_id",this.user.getAccount().getId());
        startActivity(intent);
    }
}