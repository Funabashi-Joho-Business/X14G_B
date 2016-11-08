package jp.ac.chiba_fjb.x14b_b.daichan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import jp.ac.chiba_fjb.libs.GoogleDrive;


public class MainActivity extends AppCompatActivity  {

    private GoogleDrive mDrive;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, MonitoringService.class));
            }
        });
        findViewById(R.id.buttonStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               stopService(new Intent(MainActivity.this, MonitoringService.class));
            }
        });

        mDrive = new GoogleDrive(this);


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_fragment,new FragmentOption());
        ft.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDrive.onActivityResult(requestCode,resultCode,data);
    }
}
