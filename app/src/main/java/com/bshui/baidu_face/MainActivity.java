package com.bshui.baidu_face;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.Group;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.PreferencesUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Handler handler;
    private Button featureSetBtn;//特征模型设置
    private Button imageMatchBtn;//图片VS图片
    private Button userGroundManagerBtn;//用户组管理
    private Button videoIdentifyBtn;//视频流VS人脸库(1:N)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());
        featureSetBtn = (Button)findViewById(R.id.feature_setting_btn);
        imageMatchBtn = (Button)findViewById(R.id.image_match_image_btn);
        userGroundManagerBtn = (Button)findViewById(R.id.user_groud_manager_btn);
        videoIdentifyBtn = (Button)findViewById(R.id.video_identify_faces_btn);


        featureSetBtn.setOnClickListener(this);
        imageMatchBtn.setOnClickListener(this);
        userGroundManagerBtn.setOnClickListener(this);
        videoIdentifyBtn.setOnClickListener(this);

        PreferencesUtil.initPrefs(this);
        //使用人脸1:n时使用
        DBManager.getInstance().init(this);

        //采用默认参数进行SDK初始化
        FaceSDKManager.getInstance().init(this);
        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                toast("sdk init start");

            }

            @Override
            public void initSuccess() {
                toast("sdk init success");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                toast("sdk init Fail");

            }
        });


    }

    private void toast (final String text){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        text, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.feature_setting_btn:
                Intent intent = new Intent(MainActivity.this,
                        FeatureSettingActivity.class);
                startActivity(intent);
                break;

            case R.id.image_match_image_btn:
                Intent intent1 = new Intent(MainActivity.this,ImageMatchImageActivity.class);
                startActivity(intent1);
                break;

            case R.id.user_groud_manager_btn:
                Intent intent2 = new Intent(MainActivity.this,UserGroupManagerActivity.class);
                startActivity(intent2);
                break;
            case R.id.video_identify_faces_btn:
                showSingleAlertDialog();

                break;
        }
    }

    private AlertDialog alertDialog;
    private String[] items;

    public void showSingleAlertDialog() {

        List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
        if (groupList.size() <= 0) {
            Toast.makeText(this, "还没有分组，请创建分组并添加用户", Toast.LENGTH_SHORT).show();
            return;
        }
        items = new String[groupList.size()];
        for (int i = 0; i < groupList.size(); i++) {
            Group group = groupList.get(i);
            items[i] = group.getGroupId();
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择分组groupID");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                Toast.makeText(MainActivity.this, items[index], Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, RgbVideoIdentityActivity.class);
                intent.putExtra("group_id", items[index]);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        alertDialog = alertBuilder.create();
        alertDialog.show();
    }
}
