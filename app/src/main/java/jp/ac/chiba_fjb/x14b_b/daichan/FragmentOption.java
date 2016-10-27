package jp.ac.chiba_fjb.x14b_b.daichan;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOption extends Fragment {


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

        CameraPreview cameraPreview = new CameraPreview();
        cameraPreview.open(0);
        List<Camera.Size> sizes = cameraPreview.getPreviewSizes();

        Spinner spinner = (Spinner) getView().findViewById(R.id.spinnerPreviewSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item);

        for(Camera.Size s : sizes){
            adapter.add(String.format("%d×%d",s.width,s.height));
        }
        spinner.setAdapter(adapter);
        cameraPreview.close();

    }

}
