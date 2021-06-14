package com.trinhthinh.caro.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trinhthinh.caro.LoginActivity;
import com.trinhthinh.caro.R;
import com.trinhthinh.caro.dao.AccountDAO;
import com.trinhthinh.caro.model.Account;

public class SettingDialog {
    private final Activity activity;
    private final Dialog dialog;
    private Button logout,close;
    private TextView id;
    private final Account accountSignIn;
    private final AccountDAO accountDAO;

    public SettingDialog(Activity activity,Account accountSignIn) {
        this.activity = activity;
        this.accountSignIn= accountSignIn;
        this.accountDAO = new AccountDAO(this.activity.getApplicationContext());
        this.dialog = new Dialog(this.activity);
        this.dialog.setContentView(R.layout.setting_layout);
        this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.id = this.dialog.findViewById(R.id.setting_id);
        this.id.setText(this.accountSignIn.getId());
        this.logout = this.dialog.findViewById(R.id.setting_logout);
        this.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });
        this.close = this.dialog.findViewById(R.id.setting_close);
        this.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void doLogout(){
        if(this.accountSignIn instanceof Account){
            this.accountSignIn.setLogin(false);
            this.accountDAO.saveAccount(accountSignIn);
            Intent intent = new Intent(this.activity
                    .getApplicationContext(), LoginActivity.class);
            this.activity.startActivity(intent);
        }
    }

    public void show(){
        this.dialog.show();
    }
}
