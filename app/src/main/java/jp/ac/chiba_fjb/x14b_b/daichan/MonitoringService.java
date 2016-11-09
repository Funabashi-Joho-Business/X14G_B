package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import to.pns.lib.Notify;

/**
 * Created by oikawa on 2016/10/26.
 */

public class MonitoringService extends Service {
    private Notify mNotify;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //ステータスバー表示用
        mNotify = new Notify(this,MainActivity.class,R.layout.status_layout,R.mipmap.ic_launcher);
        mNotify.setRemoteText(R.id.textTitle,getString(R.string.app_name));
        mNotify.setRemoteImage(R.id.imageNotify, R.mipmap.ic_launcher, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotify.output("サービス開始");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        mNotify.release();
        super.onDestroy();
    }
}
