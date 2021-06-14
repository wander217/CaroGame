package com.trinhthinh.caro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.trinhthinh.caro.adapter.RoomAdapter;
import com.trinhthinh.caro.dialog.CreateRoomDialog;
import com.trinhthinh.caro.model.Room;
import com.trinhthinh.caro.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListRoomActivity extends AppCompatActivity {
    private ImageView back;
    private RecyclerView list;
    private TextView money;
    private TextInputEditText searchBox;
    private Button searchButton,createRoomButton;
    private CreateRoomDialog createRoomDialog;
    private List<Room> roomList;
    private User user;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room);
        init();
    }

    private void init(){
        this.database = FirebaseDatabase
                .getInstance(ConstantValue.DATABASE_URL);
        Intent userIntent = getIntent();
        if(userIntent instanceof Intent){
            this.user = (User) userIntent.getSerializableExtra("user");
        }
        this.money = findViewById(R.id.list_room_money);
        this.money.setText(String.valueOf(this.user.getInfo().getMoney()));
        this.back = findViewById(R.id.list_room_back);
        this.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRoomActivity.this,HomeActivity.class);
                intent.putExtra("account",user.getAccount());
                startActivity(intent);
            }
        });
        this.createRoomDialog = new CreateRoomDialog(this,this.user);
        this.createRoomButton = findViewById(R.id.list_room_create_room);
        this.createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoomDialog.show();
            }
        });
        this.list = findViewById(R.id.list_room_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        this.list.setLayoutManager(linearLayoutManager);
        this.roomList = new ArrayList<>();
        RoomAdapter roomAdapter = new RoomAdapter(this.roomList,this, this.user);
        this.list.setAdapter(roomAdapter);

        this.searchBox = findViewById(R.id.list_room_search_box);
        this.searchButton = findViewById(R.id.list_room_search_button);
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchData = searchBox.getText().toString();
                if(!(searchData instanceof String)) return;
                loadRoom(roomAdapter,searchData);
            }
        });
        loadRoom(roomAdapter,"");
    }

    private void loadRoom(RoomAdapter roomAdapter,String searchData){
        DatabaseReference reference = database
                .getReference("rooms")
                .child("playing_room");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<HashMap<String,Room>> genericTypeIndicator
                        = new GenericTypeIndicator<HashMap<String, Room>>() {};
                HashMap<String,Room> tmp = snapshot.getValue(genericTypeIndicator);
                roomList.clear();
                if(!(tmp instanceof HashMap)) return;
                for(String key:tmp.keySet()){
                    Room r = tmp.get(key);
                    r.setId(key);
                    if(!r.isDelete()&& r.getName().contains(searchData)){
                        roomList.add(r);
                    }
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}