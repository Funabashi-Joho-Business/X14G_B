package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oikawa on 2016/11/10.
 */
public class Permission{
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
