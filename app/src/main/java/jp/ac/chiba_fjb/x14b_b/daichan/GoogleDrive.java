package jp.ac.chiba_fjb.x14b_b.daichan;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by oikawa on 2016/09/26.
 */

public class GoogleDrive implements  GoogleApiClient.OnConnectionFailedListener, ResultCallback<DriveApi.DriveContentsResult> {
    public class Folder{
        private DriveFolder mFolder;

        public Folder(){
            if(mGoogleApiClient.isConnected())
                mFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        }
        public Folder(DriveFolder folder){
            mFolder = folder;
        }
        public void uploadBitmap(String fileName,Bitmap bitmap){
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileName)
                    .setMimeType("image/jpg")
                    .setStarred(false).build();

            DriveApi.DriveContentsResult result = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
            DriveContents contents = result.getDriveContents();
            try {
                OutputStream fos = contents.getOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                // 保存処理終了
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mFolder.createFile(mGoogleApiClient, changeSet, contents).await();

        }
        public Folder createFolder(String name){
            //既存のフォルダを検索
            Folder f = getFolder(name);
            if(f != null)
                return f;
            //新規作成
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(name).build();
            PendingResult<DriveFolder.DriveFolderResult> a = mFolder.createFolder(mGoogleApiClient, changeSet);
            DriveFolder.DriveFolderResult result = a.await();
            if(!result.getStatus().isSuccess())
                return null;
            return new Folder(result.getDriveFolder());
        }
        public Folder getFolder(String name){
            Query q = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder")).addFilter(Filters.eq(SearchableField.TITLE, name)).build();
            PendingResult<DriveApi.MetadataBufferResult> result = mFolder.queryChildren(mGoogleApiClient,q);
            DriveApi.MetadataBufferResult values = result.await();

            if(values.getMetadataBuffer().getCount() == 0)
                return null;
            return new Folder(values.getMetadataBuffer().get(0).getDriveId().asDriveFolder());
        }
        public void query(){

            PendingResult<DriveApi.MetadataBufferResult> result = mFolder.listChildren(mGoogleApiClient);
            DriveApi.MetadataBufferResult values = result.await();

            int count = values.getMetadataBuffer().getCount();
            for(int i = 0; i<count ;i++){
                Metadata m = values.getMetadataBuffer().get(i);
                System.out.println(m.getMimeType()+(m.getOriginalFilename() != null?m.getOriginalFilename() :m.getTitle()));
            }
            values.release();
        }

    }


    private GoogleApiClient mGoogleApiClient;
    FragmentActivity mActivity;
    public GoogleDrive(FragmentActivity con){
        mGoogleApiClient = new GoogleApiClient.Builder(con)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .enableAutoManage(con, this)
                .build();
        mActivity = con;
    }

    boolean connect(){
         mGoogleApiClient.connect();
        //DriveApi.DriveContentsResult result = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
        //if(!result.getStatus().isSuccess())
        //    return false;

        return true;
    }
    public void disconnect() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        mGoogleApiClient.stopAutoManage(mActivity);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
        if(driveContentsResult.getStatus().isSuccess()){
            Drive.DriveApi.requestSync(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {

                }
            });
        }
    }

    Folder getFolder(){
        return new Folder();
    }
    boolean isConnected(){
        return mGoogleApiClient.isConnected();
    }

}
