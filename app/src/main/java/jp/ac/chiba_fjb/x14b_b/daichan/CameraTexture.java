package jp.ac.chiba_fjb.x14b_b.daichan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;

import static android.hardware.Camera.AutoFocusCallback;
import static android.hardware.Camera.PreviewCallback;

/**
 * Created by oikawa on 2016/11/09.
 */

public class CameraTexture implements AutoFocusCallback, PreviewCallback {
    private Context mContext;
    private Camera mCamera;
    private SurfaceTexture mTexutre;
    private int mCameraId = -1;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private CameraPreview.SaveListener mSaveListener;
    private byte[] mPreviewData;
    private int mDegrees;

    public CameraTexture(Context context){
        mContext = context;
    }

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
        mPreviewData = null;
        return true;
    }
    void setPreviewSize(int width,int height){
        mPreviewWidth = width;
        mPreviewHeight = height;
    }
    boolean startPreview(){
        try {
            if(mCamera == null)
                return false;
            mTexutre = new SurfaceTexture(0);
            mCamera.setPreviewTexture(mTexutre);

            mCamera.setPreviewCallback(this);

            Camera.Parameters p = mCamera.getParameters();
            android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
            mCamera.getCameraInfo(mCameraId,info);

            int rotation = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                final int[] DEG = {-90, 0, 90, 180};
                mDegrees = DEG[rotation];
            }else {
                final int[] DEG = {90, 0, -90, 180};
                mDegrees = DEG[rotation];
            }

            if(mPreviewWidth != 0){



                p.setPreviewSize(mPreviewWidth,mPreviewHeight);
                mCamera.setParameters(p);
                if(rotation%2 == 0)
                    mTexutre.setDefaultBufferSize(mPreviewHeight,mPreviewWidth);
                else
                    mTexutre.setDefaultBufferSize(mPreviewWidth,mPreviewHeight);
            }




            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    boolean stopPreview(){
        if(mCamera == null)
            return false;
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
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
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix m = new Matrix(); //Bitmapの回転用Matrix
                m.setRotate(mDegrees);
                bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),m , true);


                mSaveListener.onSave(bitmap);
            }
        }
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
}
