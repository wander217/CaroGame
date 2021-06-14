package com.trinhthinh.caro.model;

import androidx.annotation.Dimension;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@IgnoreExtraProperties
public class Room implements Serializable {
    private String id,winnerId;
    private String name;
    private User user1,user2;
    private int money;
    private boolean block2way, isDelete;
    private final HashMap<String,Turn> turn;
    private boolean user1Ready,user2Ready;

    public Room() {
        this.turn = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public boolean isBlock2way() {
        return block2way;
    }

    public void setBlock2way(boolean block2way) {
        this.block2way = block2way;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public HashMap<String, Turn> getTurn() {
        return turn;
    }

    public boolean isUser1Ready() {
        return user1Ready;
    }

    public void setUser1Ready(boolean user1Ready) {
        this.user1Ready = user1Ready;
    }

    public boolean isUser2Ready() {
        return user2Ready;
    }

    public void setUser2Ready(boolean user2Ready) {
        this.user2Ready = user2Ready;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
}
