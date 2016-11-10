package jp.ac.chiba_fjb.x14b_b.daichan;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.ac.chiba_fjb.libs.GoogleDrive;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements CameraPreview.SaveListener, View.OnClickListener {


    private CameraPreview mCamera;
    private GoogleDrive mDrive;
    private boolean mUploadFlag = false;
    private int mVisibility;

    public CameraFragment() {
        // Required empty public constructor
        mCamera = new CameraPreview();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);;
        view.setOnClickListener(this);

        view.findViewById(R.id.imageSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_fragment,new FragmentOption());
                ft.commit();
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStart() {
        super.onStart();

        mDrive = new GoogleDrive(getActivity());
        mDrive.connect();

    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getActivity().getWindow().getDecorView();
        mVisibility = view.getSystemUiVisibility();
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                         | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        CameraDB db = new CameraDB(getContext());
        int cameraType = db.getSetting("CAMERA_TYPE",0);
        int cameraWidth = db.getSetting("CAMERA_WIDTH",1280);
        int cameraHeight = db.getSetting("CAMERA_HEIGHT",960);
        db.close();

        TextureView textureView = (TextureView)getView().findViewById(R.id.textureView);
        mCamera.setTextureView(textureView);
        mCamera.open(cameraType);
        mCamera.setPreviewSize(cameraWidth,cameraHeight);
        mCamera.startPreview();
        mCamera.setOnSaveListener(this);

    }
    @Override
    public void onPause() {

        View view = getActivity().getWindow().getDecorView();
        view.setSystemUiVisibility(mVisibility );

        mCamera.close();
        super.onPause();
    }



    @Override
    public void onSave(final Bitmap bitmap) {
        final String fileName;
        try {
            CameraDB db = new CameraDB(getContext());
            final int cameraQuality = db.getSetting("CAMERA_QUALITY",100);
            db.close();

            String path = getActivity().getCacheDir().getPath();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            fileName = path+"/"+sdf.format(new Date()) + ".jpeg";

            FileOutputStream fos = null;
            fos = new FileOutputStream(new File(fileName));
            // jpegで保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, cameraQuality, fos);
            // 保存処理終了
            fos.close();

            //ファイルのアップロード要求
            Intent intent = new Intent(getContext(), UploadService.class);
            intent.putExtra("FILE_NAME",fileName);
            getActivity().startService(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        Snackbar.make(getView(),"保存要求", Snackbar.LENGTH_SHORT).show();
        mCamera.save();
    }
}
