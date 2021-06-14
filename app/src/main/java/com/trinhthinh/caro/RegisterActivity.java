package com.trinhthinh.caro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trinhthinh.caro.dao.AccountDAO;
import com.trinhthinh.caro.model.Account;
import com.trinhthinh.caro.model.Info;
import com.trinhthinh.caro.model.User;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText username,password,nickname;
    private ImageView avatar;
    private Button login,register;
    private ProgressDialog dialog;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private Uri imageAvatar;
    private AccountDAO dao;

    private static final int IMAGE_REQUEST_CODE =200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){
        this.imageAvatar =  Uri.parse("android.resource://com.trinhthinh.caro/drawable/avatar");
        this.avatar = findViewById(R.id.register_avatar);
        this.avatar.setImageURI(this.imageAvatar);
        this.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAvatar();
            }
        });

        this.storage =  FirebaseStorage.getInstance();
        this.auth= FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance(ConstantValue.DATABASE_URL);
        this.dao = new AccountDAO(getApplicationContext());

        this.username = findViewById(R.id.register_username);
        this.password = findViewById(R.id.register_password);
        this.nickname =  findViewById(R.id.register_nickname);
        this.login = findViewById(R.id.register_login);
        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        this.register = findViewById(R.id.register_regiter);
        this.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegister();
            }
        });
    }

    private void doRegister(){
        User user = doValidate();
        if(!(user instanceof User)) return;

        this.dialog = new ProgressDialog(RegisterActivity.this);
        this.dialog.setMessage("Đang xử lý...");
        this.dialog.show();

        this.auth
                .createUserWithEmailAndPassword(user.getAccount().getUsername(),user.getAccount().getPassword())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String id = authResult.getUser().getUid();
                        user.getAccount().setId(id);
                        createAvatar(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext()
                                ,"Tài khoản đã tồn tại!"
                                ,Toast.LENGTH_LONG).show();
                    }
                });
    }

    private User doValidate(){
        User user = new User();
        Account account = new Account();
        account.setLogin(false);
        user.setAccount(account);
        Info info = new Info();
        user.setInfo(info);
        String username = this.username.getText().toString();
        if(!username.matches("^.+@.+\\..+$")){
            this.username.setError("Email sai định dạng");
            return null;
        }
        user.getAccount().setUsername(username);
        String password =this.password.getText().toString();
        if(!password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%\\^\\-_])([a-zA-Z0-9@#$%\\^\\-_].{6,})$")){
            this.password.setError("Mật khẩu phải tối thiểu 6 chữ số " +
                    "và chứa ít nhất một chữ số , một chữ cái thường , một chữ cái hoa " +
                    "và các kí tự đặc biệt @#$%^-_");
            return null;
        }
        user.getAccount().setPassword(password);
        String nickname = this.nickname.getText().toString();
        if(nickname.isEmpty()){
            this.nickname.setError("Nickname không được để trống");
            return null;
        }
        user.getInfo().setNickname(nickname);
        return user;
    }

    private void createAvatar(User user){
        StorageReference reference = this.storage
                .getReference().child("avatars")
                .child(user.getAccount().getId());
        reference.putFile(this.imageAvatar)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        user.getInfo().setAvatarPath(uri.toString());
                                        createUserInfo(user);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {
                            e.printStackTrace();
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext()
                                    ,"Đã xảy ra lỗi hãy thử lại!"
                                    ,Toast.LENGTH_LONG).show();
                        }
                });
    }

    private void createUserInfo(User user){
        user.getInfo().setMoney(100);
        user.getInfo().setElo(1000);
        DatabaseReference reference= this.database
                .getReference().child("users")
                .child(user.getAccount().getId());
        reference.setValue(user.getInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dao.saveAccount(user.getAccount());
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext()
                                ,"Đăng kí thành công"
                                ,Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext()
                                ,"Đã xảy ra lỗi hãy thử lại!"
                                ,Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void changeAvatar(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,requestCode,data);
        if(resultCode == RESULT_OK && data instanceof Intent){
            switch (requestCode){
                case IMAGE_REQUEST_CODE:{
                    this.imageAvatar = data.getData();
                    this.avatar.setImageURI(this.imageAvatar);
                    return;
                }
            }
        }
    }
}