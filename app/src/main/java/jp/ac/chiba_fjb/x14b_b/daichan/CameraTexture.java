package jp.ac.chiba_fjb.x14b_b.daichan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

/**
 * Created by oikawa on 2016/11/09.
 */

public class CameraTexture implements Camera.AutoFocusCallback, Camera.PreviewCallback {
    private Camera mCamera;
    private SurfaceTexture mTexutre;
    private int mCameraId = -1;
    private CameraPreview.SaveListener mSaveListener;
    private byte[] mPreviewData;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mPreviewData = data;
    }


    static interface SaveListener{
        public void onSave(Bitmap bitmap);
    }
    void setOnSaveListener(CameraPreview.SaveListener l){
        mSaveListener = l;
    }
    public boolean open(int id){
        try {
            if(mCameraId == id)
                return true;
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
                mCameraId = -1;
            }

            //カメラデバイスを開く
            mCamera = Camera.open(id);
            mCamera.setPreviewCallback(this);
            if(mCamera == null)
                return false;
            mCameraId = id;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean close(){
        if(mCamera == null)
            return false;
        stopPreview();
        mCamera.release();
        mCamera = null;
        mCameraId = -1;
        return true;
    }
    boolean startPreview(){
        try {
            if(mCamera == null)
                return false;

            mTexutre = new SurfaceTexture(0);
            mCamera.setPreviewTexture(mTexutre);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    boolean stopPreview(){
        if(mCamera == null)
            return false;
        mCamera.stopPreview();
        return true;
    }
    public boolean save(){
        if(mCamera == null)
            return false;
        if(mCamera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
            mCamera.autoFocus(this);
        else
            onAutoFocus(true,mCamera);
        return true;
    }
    @Override
    public void onAutoFocus(boolean success, Camera camera) {

        if(mPreviewData != null){
            if(mSaveListener != null){
                Camera.Parameters parameters = camera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;

                YuvImage yuv = new YuvImage(mPreviewData, parameters.getPreviewFormat(), width, height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

                byte[] bytes = out.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                mSaveListener.onSave(bitmap);
            }
        }
    }
}
