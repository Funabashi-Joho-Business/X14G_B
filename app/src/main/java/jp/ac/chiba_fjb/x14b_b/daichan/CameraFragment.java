package jp.ac.chiba_fjb.x14b_b.daichan;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements View.OnTouchListener, CameraPreview.SaveListener {


    private CameraPreview mCamera;
    private GoogleDrive mDrive;
    private boolean mUploadFlag = false;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);;
        view.setOnTouchListener(this);
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();



        mDrive = new GoogleDrive(getActivity());
        mDrive.connect();

    }

    @Override
    public void onStop() {

        mDrive.disconnect();
        super.onStop();
    }
    @Override
    public void onResume() {
        super.onResume();

        mCamera = new CameraPreview();
        TextureView textureView = (TextureView)getView().findViewById(R.id.textureView);
        mCamera.setTextureView(textureView);
        mCamera.open(0);
        mCamera.startPreview();
        mCamera.setOnSaveListener(this);

    }
    @Override
    public void onPause() {
        mCamera.close();
        super.onPause();
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mCamera.save();
        return false;
    }



    @Override
    public void onSave(final Bitmap bitmap) {
        final String fileName;
        try {
            String path = getActivity().getCacheDir().getPath();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            fileName = path+"/"+sdf.format(new Date()) + ".jpeg";

            FileOutputStream fos = null;
            fos = new FileOutputStream(new File(fileName));
            // jpegで保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // 保存処理終了
            fos.close();

            //ファイルのアップロード要求
            Intent intent = new Intent(getContext(), UploadService.class);
            intent.putExtra("FILE_NAME",fileName);
            getActivity().startService(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        new Thread(){
            @Override
            public void run() {
                super.run();

                if(mDrive.isConnected()) {
                    if(!mUploadFlag) {
                        mUploadFlag = true;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), filename + "を保存中", Snackbar.LENGTH_SHORT).show();
                            }
                        });


                        GoogleDrive.Folder f = mDrive.getFolder().createFolder("CamData");
                        f.uploadBitmap(filename, bitmap);
                        System.out.println("出力");
                        mUploadFlag = false;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), filename + "を保存完了", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), "保存中の為、撮影をキャンセルしました", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }


                }else
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(getView(),"ドライブに接続されていません", Snackbar.LENGTH_SHORT).show();
                        }
                    });


            }
        }.start();*/
    }
}
