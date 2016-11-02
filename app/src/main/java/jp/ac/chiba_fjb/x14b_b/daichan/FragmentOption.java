package jp.ac.chiba_fjb.x14b_b.daichan;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOption extends Fragment {
    List<Camera.Size> mPreviewSizes;

    public FragmentOption() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_option, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        CameraDB db = new CameraDB(getContext());
        int cameraType = db.getSetting("CAMERA_TYPE",0);
        int cameraVector = db.getSetting("CAMERA_VECTOR",0);
        String cameraName = db.getSetting("CAMERA_NAME","CAMERA1");
        db.close();

        RadioGroup radioGroup = (RadioGroup)getView().findViewById(R.id.CameraType);
        radioGroup.check(cameraType==0?R.id.radioCamera0:R.id.radioCamera1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                CameraDB db = new CameraDB(getContext());
                db.setSetting("CAMERA_TYPE",checkedId==R.id.radioCamera0?0:1);
                db.close();
                updatePreviewSize();
            }
        });

        Spinner spinner = (Spinner) getView().findViewById(R.id.spinnerPreviewSize);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Camera.Size size = mPreviewSizes.get(position);
                CameraDB db = new CameraDB(getContext());
                db.setSetting("CAMERA_WIDTH",size.width);
                db.setSetting("CAMERA_HEIGHT",size.height);
                db.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updatePreviewSize();



    }

    void updatePreviewSize(){

        Camera camera = Camera.open(0);
        camera.release();
        camera = Camera.open(1);
        camera.release();
        camera = Camera.open(0);
        camera.release();

        CameraDB db = new CameraDB(getContext());
        int cameraType = db.getSetting("CAMERA_TYPE",0);
        int cameraWidth = db.getSetting("CAMERA_WIDTH",0);
        int cameraHeight = db.getSetting("CAMERA_HEIGHT",0);
        db.close();

        System.out.println(cameraType);
        //Camera camera = Camera.open(cameraType);
       // Camera.Parameters p = camera.getParameters();
        //camera.release();


        //mPreviewSizes = p.getSupportedPreviewSizes();


//        if(mPreviewSizes != null){
//            int selectIndex = 0;
//
//            Spinner spinner = (Spinner) getView().findViewById(R.id.spinnerPreviewSize);
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item);
//            int i = 0;
//            for(Camera.Size s : mPreviewSizes){
//                adapter.add(String.format("%d×%d",s.width,s.height));
//                if(cameraWidth ==s.width &&  cameraHeight == s.height)
//                    selectIndex = i;
//                i++;
//            }
//            spinner.setAdapter(adapter);
//            spinner.setSelection(selectIndex);
//        }


    }
}
