package com.trinhthinh.caro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.trinhthinh.caro.model.Account;

public class AccountDAO extends SQLiteOpenHelper {
    private static final String DATABASE= "caro_game";
    private static final String ID = "id";
    private static final String USERNAME="username";
    private static final String PASSWORD="password";
    private static final String ISLOGIN="is_login";
    private static final int version =1;

    public AccountDAO(@Nullable Context context) {
        super(context, DATABASE, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDB = "CREATE TABLE "+DATABASE+"("
                +ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                +USERNAME+" VARCHAR(255) NOT NULL,"
                +PASSWORD+" VARCHAR(255) NOT NULL,"
                +ISLOGIN+" BOOLEAN NOT NULL)";
        db.execSQL(createDB);
    }

    public boolean saveAccount(Account account){
        ContentValues values = new ContentValues();
        values.put(USERNAME,account.getUsername());
        values.put(PASSWORD,account.getPassword());
        values.put(ISLOGIN,account.isLogin());
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(DATABASE,null,values)>0;
    }

    public Account getAccount(){
        Account account = new Account();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor= database.query(DATABASE,null,null
                ,null,null,null,null);
        if(cursor instanceof Cursor && cursor.moveToFirst()){
            do{
                if(cursor.isLast()){
                    account.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME)));
                    account.setPassword(cursor.getString(cursor.getColumnIndex(PASSWORD)));
                    account.setLogin(cursor.getInt(cursor.getColumnIndex(ISLOGIN))==1);
                }
            }while (cursor.moveToNext());
        }
        return account;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
