package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.IntentService;
import android.content.Intent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

import jp.ac.chiba_fjb.libs.GoogleDrive;
import to.pns.lib.LogService;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UploadService extends IntentService {
    private Queue<String> mUploadQueue = new ArrayDeque<String>();
    private Thread mThread;
    private GoogleDrive mDrive;
    public UploadService() {
        super("UploadService");
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {


            final String action = intent.getAction();
            final String param1 = intent.getStringExtra("FILE_NAME");
            mUploadQueue.add(param1);

            if(mThread == null || !mThread.isAlive()){
                mThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();



                        //カメラ名を取得
                        CameraDB db = new CameraDB(UploadService.this);
                        String cameraName = db.getSetting("CAMERA_NAME","CAMERA1");
                        db.close();
                        //日付を文字列化
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        String dayString = sdf.format(new Date());

                        mDrive = new GoogleDrive(UploadService.this);
                        String fileName;
                        while((fileName = mUploadQueue.poll()) != null){

                            File file = new File(fileName);
                            String upname = String.format("/ComData/%s/%s/",cameraName,dayString,file.getName());
                            LogService.output(getApplicationContext(),"送信:"+file.getName());
                            String id = mDrive.upload(upname,fileName,"image/jpeg");
                            if(id != null){
                                System.out.println(fileName+":"+id+"出力");
                                file.exists();
                                LogService.output(getApplicationContext(),"完了:"+file.getName());
                            }
                            else
                                LogService.output(getApplicationContext(),"エラー:"+file.getName());

                        }

                    }
                };
                mThread.start();
            }
        }
    }

}
