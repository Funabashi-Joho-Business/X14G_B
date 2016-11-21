package jp.ac.chiba_fjb.libs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;


/**
 * Created by oikawa on 2016/09/26.
 */

public class GoogleDrive extends GoogleAccount {

    interface OnConnectListener{
        public void onConnected(boolean flag);
    }
    private OnConnectListener mListener;
    private Drive mDrive;
    private String mRootId;
    Context mActivity;
    public GoogleDrive(Context con){
        super(con,null);

        FragmentActivity activity = null;
        if(con instanceof FragmentActivity)
            activity = (FragmentActivity)con;
        mActivity = con;

        mDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), getCredential()).build();
    }
    public void setOnConnectedListener(OnConnectListener listener){
        mListener = listener;
    }
    public boolean connect(){
        requestAccount();
        return true;
    }

    @Override
    protected void onExec() throws IOException {
        mRootId = mDrive.files().get("root").setFields("id").execute().getId();
        if(mListener != null)
            mListener.onConnected(mRootId != null);
    }

    @Override
    protected void onError() {
        super.onError();
        if(mListener != null)
            mListener.onConnected(false);
    }

    public String getRootId(){
        try {
            if(mRootId == null)
                mRootId = mDrive.files().get("root").setFields("id").execute().getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mRootId;
    }
    FileList getFileList(String id){
        try {
            return mDrive.files().list().setQ(String.format("'%s' in parents",mRootId)).execute();
        } catch (IOException e) {
            return null;
        }
    }
    public String getFolderId(String parent,String name){
        try {
            FileList list = mDrive.files().list().setQ(String.format("'%s' in parents and name='%s'", parent, name)).execute();
            if(list.getFiles().size() > 0)
                return list.getFiles().get(0).getId();
        } catch (IOException e) {}
        return null;
    }
    public String getFolderId(String name,boolean cflag){
        String[] folders = name.split("/", 0);
        String id = getRootId();
        for(String f : folders){
            if(f.length() == 0)
                continue;
            String id2 = getFolderId(id,f);
            if(id2 == null && cflag)
                id = createFolder(id,f);
            else
                id = id2;

        }
        return id;
    }
    public String createFolder(String id,String name){
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if(id != null)
            fileMetadata.setParents(Collections.singletonList(id));
        try {
            File file = mDrive.files().create(fileMetadata).setFields("id, parents").execute();
            if(file != null)
                return file.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String upload(String dest,String src,String type){
        try {
            java.io.File fileDest = new java.io.File(dest);
            String pid = getFolderId(fileDest.getParent(),true);

            File fileMetadata = new File();
            fileMetadata.setName(fileDest.getName());
            fileMetadata.setMimeType(type);
            if(pid != null)
                fileMetadata.setParents(Collections.singletonList(pid));


            java.io.File filePath = new java.io.File(src);
            FileContent mediaContent = new FileContent(type, filePath);
            File file = mDrive.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            return file.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Bitmap downloadImage(String id){
        try {
            HttpResponse response = mDrive.files().get(id).executeMedia();
            Bitmap bitmapm = BitmapFactory.decodeStream(response.getContent());
            return bitmapm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
