package com.bshui.baidu_face;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.aip.utils.PreferencesUtil;
import com.bshui.baidu_face.utils.GlobalFaceTypeModel;

public class FeatureSettingActivity extends AppCompatActivity {
    private RadioButton rbRecongnizeLive;
    private RadioButton rbRecongnizeIdPhoto;
    private RadioGroup rgModel;
    private Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_setting);

        rbRecongnizeLive = (RadioButton)findViewById(R.id.rb_recognize_live);
        rbRecongnizeIdPhoto = (RadioButton)findViewById(R.id.rb_recognize_id_photo);
        rgModel = (RadioGroup)findViewById(R.id.rg_model);
        confirmBtn = (Button)findViewById(R.id.confirm_btn);

        int modelType = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL,
                GlobalFaceTypeModel.RECOGNIZE_LIVE);
        if(modelType == GlobalFaceTypeModel.RECOGNIZE_LIVE){
            rbRecongnizeLive.setChecked(true);
        }else if(modelType == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO){
            rbRecongnizeIdPhoto.setChecked(true);
        }

        //设置不使用活体检测
        PreferencesUtil.putInt(GlobalFaceTypeModel.TYPE_LIVENSS,
                GlobalFaceTypeModel.TYPE_NO_LIVENSS);

        rgModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_recognize_live:
                        PreferencesUtil.putInt(
                                GlobalFaceTypeModel.TYPE_MODEL,
                                GlobalFaceTypeModel.RECOGNIZE_LIVE);
                        break;
                    case R.id.rb_recognize_id_photo:
                        PreferencesUtil.putInt(
                                GlobalFaceTypeModel.TYPE_MODEL,
                                GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO);
                        break;
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }
}
