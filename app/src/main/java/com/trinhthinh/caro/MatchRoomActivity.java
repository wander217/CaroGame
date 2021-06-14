package com.trinhthinh.caro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.adapter.TableAdapter;
import com.trinhthinh.caro.model.Cell;
import com.trinhthinh.caro.model.Room;
import com.trinhthinh.caro.model.Turn;
import com.trinhthinh.caro.model.User;
import com.trinhthinh.caro.task.AvatarTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchRoomActivity extends AppCompatActivity {
    private ImageView avatar1;
    private TextView nickname1,elo1;
    private CardView turnTag1;

    private ImageView avatar2;
    private TextView nickname2,elo2;
    private CardView turnTag2;

    private TextView content,message;

    private ConstraintLayout result;
    private Button playAgain;
    private TextView eloResult,coinResult;

    private RecyclerView table;
    private Room room;
    private User user;
    private String userId;
    private FirebaseDatabase database;
    private List<Cell> cellList;
    private int row,column;
    private TableAdapter tableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_room);
        init();
    }

    private void init(){
        String roomId=null;
        Intent roomIntent = getIntent();
        if(roomIntent instanceof Intent){
            roomId = roomIntent.getStringExtra("room_id");
            this.userId = roomIntent.getStringExtra("user_id");
        }
        this.database = FirebaseDatabase
                .getInstance(ConstantValue.DATABASE_URL);

        this.avatar1 = findViewById(R.id.match_room_avatar1);
        this.nickname1 =  findViewById(R.id.match_room_nickname1);
        this.elo1 = findViewById(R.id.match_room_elo1);
        this.turnTag1 = findViewById(R.id.match_room_clock1);

        this.avatar2 = findViewById(R.id.match_room_avatar2);
        this.nickname2 =  findViewById(R.id.match_room_nickname2);
        this.elo2 = findViewById(R.id.match_room_elo2);
        this.turnTag2 = findViewById(R.id.match_room_clock2);

        this.result = findViewById(R.id.match_room_result);
        this.result.setVisibility(View.INVISIBLE);
        this.eloResult = findViewById(R.id.match_room_elo_result);
        this.coinResult = findViewById(R.id.match_room_money_result);
        this.playAgain= findViewById(R.id.match_room_play_again_button);
        this.playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnRoom();
            }
        });

        this.table = findViewById(R.id.match_room_table);
        this.row=20;
        this.column =13;
        GridLayoutManager gridLayoutManager
                = new GridLayoutManager(getApplicationContext(),this.column);
        this.table.setLayoutManager(gridLayoutManager);
        this.cellList = new ArrayList<>();
        for(int i=0;i<this.row;i++){
            for(int j=0;j<this.column;j++){
                Cell cell = new Cell();
                cell.setType("");
                cell.setX(j);
                cell.setY(i);
                this.cellList.add(cell);
            }
        }
        tableAdapter = new TableAdapter(cellList, getColor(R.color.x_color)
                , getColor(R.color.o_color), "x", false, userId, roomId);
        table.setAdapter(tableAdapter);
        this.content = findViewById(R.id.match_room_result_content);
        this.message =  findViewById(R.id.match_room_result_message);
        loadData(roomId);
    }

    private void changeTimer(CardView t1,CardView t2){
        t1.setVisibility(View.INVISIBLE);
        t2.setVisibility(View.VISIBLE);
    }

    private void loadData(String roomId){
        ProgressDialog dialog = new ProgressDialog(MatchRoomActivity.this);
        dialog.show();
        this.database
                .getReference("rooms")
                .child("playing_room").child(roomId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        room = dataSnapshot.getValue(Room.class);
                        if(!(room instanceof Room)){ return; }
                        System.out.println("GET");
                        if (userId.equals(room.getUser1().getAccount().getId())) {
                            setUser1(room.getUser2());
                            setUser2(room.getUser1());
                            tableAdapter.setType("x");
                            tableAdapter.setClickable(true);
                            changeTimer(turnTag1, turnTag2);
                        } else {
                            setUser1(room.getUser1());
                            setUser2(room.getUser2());
                            tableAdapter.setType("o");
                            tableAdapter.setClickable(false);
                            changeTimer(turnTag2, turnTag1);
                        }
                        dialog.dismiss();
                    }
                });
        this.database
                .getReference("rooms")
                .child("playing_room").child(roomId).child("turn")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<HashMap<String,Turn>> indicator
                                = new GenericTypeIndicator<HashMap<String, Turn>>() {};
                        HashMap<String,Turn> map = snapshot.getValue(indicator);
                        if(!(map instanceof Map)) return;
                        System.out.println("room size: "+map.size());
                        Turn lastTurn = null;
                        for(Turn turn: map.values()){
                            lastTurn = turn;
                            Cell cell = cellList.get(lastTurn.getPos());
                            cell.setType(lastTurn.getType());
                        }
                        tableAdapter.notifyDataSetChanged();
                        System.out.println(userId+":"+!lastTurn.getUserId().equals(userId));
                        if(!lastTurn.getUserId().equals(userId)){
                            changeTimer(turnTag1, turnTag2);
                            tableAdapter.setClickable(true);
                        }else {
                            changeTimer(turnTag2, turnTag1);
                            tableAdapter.setClickable(false);
                        }
                        checkWinner();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void checkWinner(){
        Cell[][] cells = new Cell[this.row][this.column];
        for(Cell cell:cellList){
            cells[cell.getY()][cell.getX()] =cell;
        }

        int[] left = new int[]{1,0,1,1};
        int[] right = new int[]{0,1,1,-1};
        for(int i =0 ;i<this.row;i++){
            for(int j=0;j<this.column;j++){
                if(!cells[i][j].getType().isEmpty()){
                    for(int k=0;k<4;k++){
                        int count =0,x=i,y=j;
                        while(x>=0&&x<this.row&&y>=0&&y<this.column&&
                                cells[i][j].getType().equals(cells[x][y].getType())){
                            count++;
                            x+=left[k];
                            y+=right[k];
                        }
                        if (count==5){
                            showResult(cells[i][j].getType().equals(this.tableAdapter.getType()));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void setUser1(User user1){
        AvatarTask roomTask = new AvatarTask(this.avatar1,user1,null);
        roomTask.execute();
        this.nickname1.setText(user1.getInfo().getNickname());
        this.elo1.setText(String.valueOf(user1.getInfo().getElo()));
    }

    private void setUser2(User user2){
        AvatarTask roomTask = new AvatarTask(this.avatar2,user2,null);
        roomTask.execute();
        this.nickname2.setText(user2.getInfo().getNickname());
        this.elo2.setText(String.valueOf(user2.getInfo().getElo()));
    }

    private void returnRoom(){
        finishAffinity();
    }

    private void showResult(boolean win){
        this.database
                .getReference("rooms")
                .child("playing_room")
                .child(this.room.getId())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Room room = currentData.getValue(Room.class);
                        if(room == null){
                            return Transaction.success(currentData);
                        }
                        room.setWinnerId(win?userId:room.getWinnerId());
                        room.setDelete(true);
                        currentData.setValue(room);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable  DatabaseError error, boolean committed, @Nullable  DataSnapshot currentData) {
                        int elo = win? 10:-10;
                        eloResult.setText("Elo:"+elo);
                        int money = win? room.getMoney():-room.getMoney();
                        coinResult.setText("Coin:"+money);
                        content.setText(win?"Chiến thắng":"Thua cuộc");
                        message.setText(win?"Bạn đã thắng":"Bạn đã thua");
                        result.setVisibility(View.VISIBLE);
                        database.getReference("users")
                                .child(userId).runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull  MutableData currentData) {
                                        User user1 = currentData.getValue(User.class);
                                        if(user1 == null){
                                            return Transaction.success(currentData);
                                        }
                                        user = user1;
                                        int elo = user.getInfo().getElo();
                                        user.getInfo().setElo(win?(elo+10):(elo-10));
                                        int money = user.getInfo().getMoney();
                                        user.getInfo().setMoney(win?(money+room.getMoney()):money-room.getMoney());
                                        currentData.setValue(room);
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable  DatabaseError error, boolean committed, @Nullable  DataSnapshot currentData) {

                                    }
                                });
                    }
                });

    }
}