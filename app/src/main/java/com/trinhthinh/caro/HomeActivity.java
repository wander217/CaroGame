package com.trinhthinh.caro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trinhthinh.caro.adapter.HomeAdapter;
import com.trinhthinh.caro.dao.AccountDAO;
import com.trinhthinh.caro.dialog.SettingDialog;
import com.trinhthinh.caro.model.Account;

public class HomeActivity extends AppCompatActivity {
    private ViewPager homePager;
    private BottomNavigationView homeNav;
    private SettingDialog setting;
    private AccountDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initPageFrame();
    }

    private void initPageFrame(){
        this.dao = new AccountDAO(HomeActivity.this);
        Intent accountIntent = getIntent();
        Account account = (Account) accountIntent.getSerializableExtra("account");
        if(!(account instanceof Account)){
            account.setLogin(false);
            this.dao.saveAccount(account);
            finish();
        }
        this.homePager = findViewById(R.id.home_pager);
        this.homeNav = findViewById(R.id.home_nav);
        this.homeNav.setSelectedItemId(R.id.play_button);
        HomeAdapter homeAdapter = new HomeAdapter(getSupportFragmentManager(),account);
        this.homePager.setAdapter(homeAdapter);
        this.setting =  new SettingDialog(this,account);
        this.homeNav.setOnNavigationItemSelectedListener(new BottomNavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.rank_button:{
                        homePager.setCurrentItem(0);
                        return true;
                    }
                    case R.id.setting_button:{
                        setting.show();
                        return false;
                    }
                    case R.id.play_button:{
                        homePager.setCurrentItem(1);
                        return true;
                    }
                }
                return false;
            }
        });
        this.homePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:{
                        homeNav.setSelectedItemId(R.id.rank_button);
                        break;
                    }
                    default:{
                        homeNav.setSelectedItemId(R.id.play_button);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        this.homePager.setCurrentItem(1);
    }
}