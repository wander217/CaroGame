package com.trinhthinh.caro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trinhthinh.caro.ConstantValue;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.model.Cell;
import com.trinhthinh.caro.model.Turn;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableHolder> {
    private final List<Cell> cellList;
    private final int xColor;
    private final int yColor;
    private String type;
    private boolean clickable;
    private final String userId;
    private final String roomId;
    private FirebaseDatabase database;

    public TableAdapter(List<Cell> cellList, int xColor, int yColor
            , String type, boolean clickable, String userId, String roomId) {
        this.cellList = cellList;
        this.xColor = xColor;
        this.yColor = yColor;
        this.type = type;
        this.clickable = clickable;
        this.userId = userId;
        this.roomId = roomId;
        this.database = FirebaseDatabase
                .getInstance(ConstantValue.DATABASE_URL);
    }

    @Override
    public TableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_layout,parent,false);
        return new TableHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TableHolder holder, int position) {
        Cell turn = cellList.get(position);
        holder.cell.setText(turn.getType());
        if(turn.getType().equals("x")){
            holder.cell.setTextColor(xColor);
        }else {
            holder.cell.setTextColor(yColor);
        }
        holder.cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickable&&turn.getType().isEmpty()){
                    turn.setType(type);
                    notifyItemChanged(position);
                    DatabaseReference reference = database
                            .getReference("rooms").child("playing_room")
                            .child(roomId).child("turn").push();
                    Turn clickCell = new Turn();
                    clickCell.setId(reference.getKey());
                    clickCell.setPos(position);
                    clickCell.setUserId(userId);
                    clickCell.setType(type);
                    reference.setValue(clickCell);
                    clickable=false;
                }
            }
        });
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Override
    public int getItemCount() {
        return this.cellList.size();
    }

    public class TableHolder extends RecyclerView.ViewHolder{
        private final TextView cell;

        public TableHolder(@NonNull View itemView) {
            super(itemView);
            this.cell = itemView.findViewById(R.id.cell);
        }
    }
}
