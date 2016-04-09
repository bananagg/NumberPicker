package com.gty.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.gty.picker.PickerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private PickerView mNumberPicker;
    private List<String> mNumData = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberPicker = (PickerView) findViewById(R.id.numberpicker);
        getData();
        Log.d(TAG,"====================="+mNumData.size());
        mNumberPicker.setData(mNumData);
        mNumberPicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                Toast.makeText(MainActivity.this,"your select "+text,Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getData(){
        for (int i=0;i<100;i++){
            mNumData.add(i<10?"0"+i:""+i);
        }
    }

}
