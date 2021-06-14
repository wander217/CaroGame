package com.trinhthinh.caro.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.trinhthinh.caro.ConstantValue;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.WaitingRoomActivity;
import com.trinhthinh.caro.model.Room;
import com.trinhthinh.caro.model.User;

public class CreateRoomDialog {
    private final Activity activity;
    private final Dialog dialog;
    private final User user;
    private Button close;

    private Button add,sub;
    private TextView coin;
    private Button yes,no;
    private Button createRoom;

    private boolean block2way = true;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private Integer countLast=0;

    public CreateRoomDialog(Activity activity, User user) {
        this.activity = activity;
        this.user = user;
        this.dialog = new Dialog(this.activity);
        this.dialog.setContentView(R.layout.create_room_layout);
        this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.close =  this.dialog.findViewById(R.id.create_room_close);
        this.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        this.coin = this.dialog.findViewById(R.id.create_room_coin);
        this.coin.setText(String.valueOf(this.user.getInfo().getMoney()));
        this.add = this.dialog.findViewById(R.id.create_room_add);
        this.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coinText = coin.getText().toString();
                int coinNumber = Integer.parseInt(coinText);
                if(coinNumber<user.getInfo().getMoney()) coinNumber++;
                coin.setText(String.valueOf(coinNumber));
            }
        });
        this.sub= this.dialog.findViewById(R.id.create_room_sub);
        this.sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coinText = coin.getText().toString();
                int coinNumber = Integer.parseInt(coinText);
                if(coinNumber!=0) coinNumber--;
                coin.setText(String.valueOf(coinNumber));
            }
        });

        this.yes = this.dialog.findViewById(R.id.create_room_yes);
        this.yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                block2way= true;
                changeColorButton(no,yes);
            }
        });
        this.no = this.dialog.findViewById(R.id.create_room_no);
        this.no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                block2way =false;
                changeColorButton(yes,no);
            }
        });

        this.progressDialog = new ProgressDialog(this.activity);
        this.database = FirebaseDatabase.getInstance(ConstantValue.DATABASE_URL);
        this.createRoom = this.dialog.findViewById(R.id.create_room_room);
        this.createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                createNewRoom();
            }
        });
    }

    private void createNewRoom(){
        this.database
                .getReference("rooms").child("count")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(@NonNull  MutableData currentData) {
                        Integer count = currentData.getValue(Integer.class);
                        if(!(count instanceof Integer)){
                            count= 0;
                        }
                        currentData.setValue(count+1);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable  DatabaseError error, boolean committed
                            , @Nullable DataSnapshot currentData) {
                        Integer count = currentData.getValue(Integer.class);
                        if(!(count instanceof Integer)){ return; }
                        makeRoom(count);
                    }
                });
    }

    private void makeRoom(int name){
        DatabaseReference reference= this.database
                .getReference("rooms")
                .child("playing_room").push();

        Room room = new Room();
        room.setId(reference.getKey());
        room.setName(String.valueOf(name));
        room.setMoney(Integer
                .parseInt(this.coin.getText().toString()));
        room.setBlock2way(block2way);
        room.setUser1(user);
        room.setDelete(false);

        reference.setValue(room)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(activity
                                .getApplicationContext(), WaitingRoomActivity.class);
                        intent.putExtra("room_id",reference.getKey());
                        intent.putExtra("user",user);
                        activity.startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(activity.getApplicationContext(),"Hãy thử lai",Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void changeColorButton(Button button1, Button button2){
        button1.setBackgroundColor(activity
                .getResources().getColor(R.color.toggle_button));
        button1.setTextColor(activity.
                getResources().getColor(R.color.black));
        button2.setBackgroundColor(activity
                .getResources().getColor(R.color.menu_button));
        button2.setTextColor(activity.
                getResources().getColor(R.color.white));
    }

    public void show(){
        this.dialog.show();
    }
}
