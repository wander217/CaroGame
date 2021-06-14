package com.trinhthinh.caro.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.trinhthinh.caro.fragment.MenuFragment;
import com.trinhthinh.caro.fragment.RankFragment;
import com.trinhthinh.caro.model.Account;

public class HomeAdapter extends FragmentStatePagerAdapter {
    private static final int fragmentNum =2;
    private final Account account;

    public HomeAdapter(@NonNull FragmentManager fm,Account account) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.account = account;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:{
                return new MenuFragment(this.account);
            }
            default:{
                return new RankFragment(this.account);
            }
        }
    }

    @Override
    public int getCount() {
        return this.fragmentNum;
    }
}
