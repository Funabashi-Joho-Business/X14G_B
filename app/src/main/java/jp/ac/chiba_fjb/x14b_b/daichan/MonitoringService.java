package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import to.pns.lib.Notify;

/**
 * Created by oikawa on 2016/10/26.
 */

public class MonitoringService extends Service implements CameraPreview.SaveListener {
    private Handler mHandler = new Handler();
    private Notify mNotify;
    private CameraPreview mCamera;
    private View mView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    Timer mTimer;
    @Override
    public void onCreate() {
        super.onCreate();
        //ステータスバー表示用
        mNotify = new Notify(this,MainActivity.class,R.layout.status_layout,R.mipmap.ic_launcher);
        mNotify.setRemoteText(R.id.textTitle,getString(R.string.app_name));
        mNotify.setRemoteImage(R.id.imageNotify, R.mipmap.ic_launcher, 0);

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        // レイアウトファイルから重ね合わせするViewを作成する
        mView = layoutInflater.inflate(R.layout.layer, null);

        // 重ね合わせするViewの設定を行う
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT);


        // WindowManagerを取得する
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        wm.addView(mView,params);

        mCamera = new CameraPreview();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotify.output("サービス開始");

        CameraDB db = new CameraDB(this);
        int cameraTimer = 30000;//Integer.parseInt(db.getSetting("CAMERA_TIMER","10"));

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CameraDB db = new CameraDB(MonitoringService.this);
                        int cameraType = db.getSetting("CAMERA_TYPE",0);
                        int cameraWidth = db.getSetting("CAMERA_WIDTH",1280);
                        int cameraHeight = db.getSetting("CAMERA_HEIGHT",960);
                        db.close();

                        TextureView textureView = (TextureView) mView.findViewById(R.id.textureView);



                        if(mCamera.open(cameraType)){
                            mCamera.setTextureView(textureView);
                            mCamera.setPreviewSize(cameraWidth,cameraHeight);
                            mCamera.startPreview();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mCamera.setOnSaveListener(MonitoringService.this);
                            mCamera.save();
                        }

                        //onTimer();
                    }
                });

            }
        }, 0, cameraTimer);


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mNotify.release();
        super.onDestroy();
    }
    public void onTimer(){



    }

    @Override
    public void onSave(Bitmap bitmap) {

        try {
            if(bitmap != null) {
                mCamera.close();

                CameraDB db = new CameraDB(this);
                final int cameraQuality = db.getSetting("CAMERA_QUALITY", 100);
                db.close();

                String path = getCacheDir().getPath();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String fileName = path + "/" + sdf.format(new Date()) + ".jpeg";

                FileOutputStream fos = null;
                fos = new FileOutputStream(new File(fileName));
                // jpegで保存
                bitmap.compress(Bitmap.CompressFormat.JPEG, cameraQuality, fos);
                // 保存処理終了
                fos.close();

                //ファイルのアップロード要求
                Intent intent = new Intent(this, UploadService.class);
                intent.putExtra("FILE_NAME", fileName);
                startService(intent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
