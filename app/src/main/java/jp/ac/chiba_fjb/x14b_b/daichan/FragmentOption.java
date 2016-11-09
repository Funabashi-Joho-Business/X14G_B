package jp.ac.chiba_fjb.x14b_b.daichan;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import jp.ac.chiba_fjb.libs.GoogleDrive;


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
        final View view = inflater.inflate(R.layout.fragment_option, container, false);
        view.findViewById(R.id.buttonPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_fragment,new CameraFragment());
                ft.commit();
            }
        });

        CameraDB db = new CameraDB(getContext());
        String cameraTimer = db.getSetting("CAMERA_TIMER","10");
        int cameraType = db.getSetting("CAMERA_TYPE",0);
        int cameraVector = db.getSetting("CAMERA_VECTOR",0);
        final int cameraQuality = db.getSetting("CAMERA_QUALITY",100);
        String cameraName = db.getSetting("CAMERA_NAME","CAMERA1");
        db.close();

        EditText editName = (EditText)view.findViewById(R.id.editName);
        editName.setText(cameraName);
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CameraDB db = new CameraDB(getContext());
                db.setSetting("CAMERA_NAME",s.toString());
                db.close();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekQuality);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView textQuality = (TextView)view.findViewById(R.id.textQuality);
                textQuality.setText(""+progress);
                CameraDB db = new CameraDB(getContext());
                db.setSetting("CAMERA_QUALITY",progress);
                db.close();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(cameraQuality);



        RadioGroup radioGroup = (RadioGroup)view.findViewById(R.id.CameraType);
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

        RadioGroup radioGroup2 = (RadioGroup)view.findViewById(R.id.CameraVector);
        radioGroup2.check(cameraType==0?R.id.radioVector0:R.id.radioVector1);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                CameraDB db = new CameraDB(getContext());
                db.setSetting("CAMERA_VECTOR",checkedId==R.id.radioVector0?0:1);
                db.close();
            }
        });

        Spinner spinner;
        //プレビューサイズの設定
        spinner = (Spinner) view.findViewById(R.id.spinnerPreviewSize);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mPreviewSizes != null) {
                    Camera.Size size = mPreviewSizes.get(position);
                    CameraDB db = new CameraDB(getContext());
                    db.setSetting("CAMERA_WIDTH", size.width);
                    db.setSetting("CAMERA_HEIGHT", size.height);
                    db.close();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        //タイマー設定
        spinner = (Spinner) view.findViewById(R.id.spinnerTimer);
        for(int i=0;i<spinner.getCount();i++){
            if(spinner.getItemAtPosition(i).equals(cameraTimer)) {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mPreviewSizes != null) {
                    String value = (String)parent.getItemAtPosition(position);
                    CameraDB db = new CameraDB(getContext());
                    db.setSetting("CAMERA_TIMER", value);
                    db.close();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        GoogleDrive drive = new GoogleDrive(getActivity());
        if(drive.getAccount() != null){
            TextView textAccount = (TextView)view.findViewById(R.id.textAccount);
            textAccount.setText(drive.getAccount());
        }
        view.findViewById(R.id.buttonAccountRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleDrive drive = new GoogleDrive(getActivity());
                drive.resetAccount();
                drive.requestAccount();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updatePreviewSize();
    }

    void updatePreviewSize(){

        CameraDB db = new CameraDB(getContext());
        int cameraType = db.getSetting("CAMERA_TYPE",0);
        int cameraWidth = db.getSetting("CAMERA_WIDTH",0);
        int cameraHeight = db.getSetting("CAMERA_HEIGHT",0);
        db.close();


        Camera camera = Camera.open(cameraType);
        Camera.Parameters p = camera.getParameters();
        camera.release();


        mPreviewSizes = p.getSupportedPreviewSizes();


        if(mPreviewSizes != null){
            int selectIndex = 0;

            Spinner spinner = (Spinner) getView().findViewById(R.id.spinnerPreviewSize);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item);
            int i = 0;
            for(Camera.Size s : mPreviewSizes){
                adapter.add(String.format("%d×%d",s.width,s.height));
                if(cameraWidth ==s.width &&  cameraHeight == s.height)
                    selectIndex = i;
                i++;
            }
            spinner.setAdapter(adapter);
            spinner.setSelection(selectIndex);
        }


    }
}
