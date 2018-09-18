package com.bshui.baidu_face;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.Group;

import java.util.List;

public class UserGroupManagerActivity extends AppCompatActivity implements View.OnClickListener{
    private Button viewGroupBtn;//用户组查看
    private Button addGroupBtn;//用户组添加
    private Button userRegBtn;//用户人脸注册

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_manager);
        DBManager.getInstance().init(getApplicationContext());

        viewGroupBtn =  (Button)findViewById(R.id.view_group_btn);
        addGroupBtn  =  (Button)findViewById(R.id.add_group_btn);
        userRegBtn   =  (Button)findViewById(R.id.user_reg_btn);

        viewGroupBtn.setOnClickListener(this);
        addGroupBtn.setOnClickListener(this);
        userRegBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        List<Group> groupList = FaceApi.getInstance().getGroupList(0,1000);
        Log.i("bshui","size:"+groupList.size()+"------");
        switch (view.getId()){
            case R.id.view_group_btn:
                if(groupList.size()<=0){
                    Toast.makeText(this, "还没有分组，请创建分组并添加用户",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(this,GroupListActivity.class);
                startActivity(intent);

                break;
            case R.id.add_group_btn:
                Intent intent1 = new Intent(this,AddGroupActivity.class);
                startActivity(intent1);
                break;
            case R.id.user_reg_btn:

                break;
        }
    }
}
