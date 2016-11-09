package jp.ac.chiba_fjb.x14b_b.daichan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ac.chiba_fjb.libs.GoogleDrive;

class Permission{
    public static interface ResultListener{
        public void onResult();
    }
    private ResultListener mListener;
    private Activity mActivity;


    private Set<String> mPermissionList = new HashSet<String>();
    public void setOnResultListener(ResultListener listener){
        mListener = listener;
    }
    public void addPermission(String permission){
        mPermissionList.add(permission);
    }
    boolean isPermissions(Activity context){
        for (String permission : mPermissionList) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }
    void requestPermissions(Activity context){
        mActivity = context;
        List<String> list = new ArrayList<String>();
        for (String permission : mPermissionList) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                list.add(permission);
        }
        if(list.size() > 0) {
           ActivityCompat.requestPermissions(context,list.toArray(new String[list.size()]) , 123);
        }
        if(mListener != null)
            mListener.onResult();
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(!isPermissions(mActivity))
            requestPermissions(mActivity);
        else
            mListener.onResult();
    }
}


public class MainActivity extends AppCompatActivity  {

    private GoogleDrive mDrive;
    private ViewPager mViewPager;
    private Permission mPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrive = new GoogleDrive(this);


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

        mPermission = new Permission();
        mPermission.setOnResultListener(new Permission.ResultListener() {
            @Override
            public void onResult() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_fragment,new FragmentOption());
                ft.commit();
            }
        });
        mPermission.addPermission( Manifest.permission.CAMERA);
        mPermission.addPermission( Manifest.permission.GET_ACCOUNTS);
        mPermission.addPermission( Manifest.permission.SYSTEM_ALERT_WINDOW);


        mPermission.requestPermissions(this);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDrive.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
