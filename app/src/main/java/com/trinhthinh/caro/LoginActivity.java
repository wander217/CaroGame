package com.trinhthinh.caro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.trinhthinh.caro.dao.AccountDAO;
import com.trinhthinh.caro.model.Account;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText username,password;
    private Button login,register;
    private AccountDAO dao;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init(){
        this.dao = new AccountDAO(getApplicationContext());
        final Account account = this.dao.getAccount();
        this.username = findViewById(R.id.login_username);
        this.username.setText(account.getUsername());
        this.password = findViewById(R.id.login_password);
        this.password.setText(account.getPassword());
        this.login =  findViewById(R.id.login_login);
        this.auth = FirebaseAuth.getInstance();
        this.dialog = new ProgressDialog(LoginActivity.this);
        this.dialog.setMessage("Đang xử lý...");
        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(account);
            }
        });
        this.register =  findViewById(R.id.login_register);
        this.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startActivity(intent);
            }
        });
        if(account.isLogin()){
            doLogin(account);
            this.dialog.show();
        }
    }

    private void doLogin(Account account){
        if(doValidate(account)){
                dialog.show();
               this.auth.signInWithEmailAndPassword(account.getUsername(),account.getPassword())
                       .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                           @Override
                           public void onSuccess(AuthResult authResult) {
                               account.setId(authResult.getUser().getUid());
                               account.setLogin(true);
                               dao.saveAccount(account);
                               dialog.dismiss();
                               Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                               intent.putExtra("account",account);
                               startActivity(intent);
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull  Exception e) {
                                dialog.dismiss();
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"Sai email hoặc mật khẩu",Toast.LENGTH_LONG)
                                        .show();
                           }
                       });
        }
    }

    private boolean doValidate(Account account){
        String username = this.username.getText().toString();
        if(!username.matches("^.+@.+\\..+$")){
            this.username.setError("Email sai định dạng");
            return false;
        }
        account.setUsername(username);
        String password =this.password.getText().toString();
        if(!password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%\\^\\-_])([a-zA-Z0-9@#$%\\^\\-_].{6,})$")){
            this.password.setError("Mật khẩu phải tối thiểu 6 chữ số " +
                    "và chứa ít nhất một chữ số , một chữ cái thường , một chữ cái hoa " +
                    "và các kí tự đặc biệt @#$%^-_");
            return false;
        }
        account.setPassword(password);
        return true;
    }
}