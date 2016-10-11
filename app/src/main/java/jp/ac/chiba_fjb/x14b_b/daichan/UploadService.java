package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


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
        mDrive = new GoogleDrive(this);
        mDrive.connect();
    }

    @Override
    public void onDestroy() {
        mDrive.disconnect();
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

            if(mThread == null || mThread.isAlive()){
                mThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        String fileName;
                        try {
                            for(int i=0;i<10000 || !mDrive.isConnected();i++)
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!mDrive.isConnected())
                            return;

                        mDrive.sync();
                        while((fileName = mUploadQueue.poll()) != null){
                            if(mDrive.isConnected()) {
                                    String upname = new File(fileName).getName();

                                    GoogleDrive.Folder f = mDrive.getFolder().createFolder("CamData");
                                    f.uploadFile(upname,fileName);
                                    System.out.println(fileName+"出力");
                                }
                        }
                    }
                };
                mThread.run();
            }
        }
    }

}
