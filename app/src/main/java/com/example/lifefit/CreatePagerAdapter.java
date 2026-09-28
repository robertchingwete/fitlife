package com.example.lifefit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CreatePagerAdapter extends FragmentStateAdapter {

    public CreatePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WorkoutTemplatesFragment();
            case 1:
                return new CustomWorkoutFragment();
            default:
                return new WorkoutTemplatesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Templates and Custom tabs
    }
}