package jp.ac.chiba_fjb.x14b_b.daichan;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import jp.ac.chiba_fjb.libs.GoogleDrive;


public class MainActivity extends AppCompatActivity  {

    private GoogleDrive mDrive;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrive = new GoogleDrive(this);


        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            String[] mTabNames = {"オプション","カメラテスト","ログ"};

            @Override
            public Fragment getItem(int position) {
                switch(position){
                    case 0:
                        return new FragmentOption();
                    case 1:
                        return new CameraFragment();
                    case 2:
                        return new FragmentLog();
                }
                return null;
            }

            @Override
            public int getCount() {
                return mTabNames.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabNames[position];
            }
        };

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDrive.onActivityResult(requestCode,resultCode,data);
    }
}
