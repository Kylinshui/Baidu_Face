package com.bshui.baidu_face;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.bshui.baidu_face.utils.GlobalFaceTypeModel;

import java.io.FileNotFoundException;
import java.util.concurrent.Executors;

public class ImageMatchImageActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView firstIv;
    private ImageView secondIv;
    private TextView  scoreIv;
    private Button firstPickFromPhoto;
    private Button firstPickFromVideo;
    private Button secondPickFromPhoto;
    private Button secondPickFromVideo;
    private Button compareBtn;

    private static final int PICK_PHOTO_FIRST = 100;
    private static final int PICK_VIDEO_FIRST = 101;
    private static final int PICK_PHOTO_SECOND = 102;
    private static final int PICK_VIDEO_SECOND = 103;

    private byte[] firstFeature  = new byte[2048];
    private byte[] secondFeature = new byte[2048];

    private volatile boolean firstFeatureFinished = false;
    private volatile boolean secondFeatureFinished = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_match_image);

        firstIv  = (ImageView)findViewById(R.id.img_first);
        secondIv = (ImageView)findViewById(R.id.img_second);
        scoreIv  = (TextView)findViewById(R.id.score_tv);

        firstPickFromPhoto = (Button)findViewById(R.id.first_pick_from_photo);
        firstPickFromVideo = (Button)findViewById(R.id.first_pick_from_video);

        secondPickFromPhoto = (Button)findViewById(R.id.second_pick_from_photo);
        secondPickFromVideo = (Button)findViewById(R.id.second_pick_from_video);

        compareBtn = (Button)findViewById(R.id.compare_btn);

        compareBtn.setOnClickListener(this);
        firstPickFromPhoto.setOnClickListener(this);
        firstPickFromVideo.setOnClickListener(this);
        secondPickFromPhoto.setOnClickListener(this);
        secondPickFromVideo.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.first_pick_from_photo:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO_FIRST);
            break;

            case R.id.first_pick_from_video:
                //当前活体策略:无活体,或RGB活体
                Intent intent3 = new Intent(this,RgbDetectActivity.class);
                startActivityForResult(intent3,PICK_VIDEO_FIRST);

            break;

            case R.id.second_pick_from_photo:
                Intent intent1 = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1,PICK_PHOTO_SECOND);
            break;

            case R.id.second_pick_from_video:
                intent = new Intent(this, RgbDetectActivity.class);
                startActivityForResult(intent, PICK_VIDEO_SECOND);

            break;

            case R.id.compare_btn:
                match();
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_PHOTO_FIRST && (data !=null && data.getData() != null)){
           Uri uri1 = ImageUtils.geturi(data, this);
           try{
               final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
               firstIv.setImageBitmap(bitmap);
               //抽取图片特征值
               syncFeature(bitmap, firstFeature, 1);

           }catch (FileNotFoundException e){
               e.printStackTrace();
           }
        }else if(requestCode == PICK_PHOTO_SECOND && (data !=null && data.getData()!=null)){
            Uri uri2 = ImageUtils.geturi(data,this);
            try{
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri2));
                secondIv.setImageBitmap(bitmap);
                //抽取图片特征值
                syncFeature(bitmap, secondFeature, 2);

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }

        }else if(requestCode == PICK_VIDEO_FIRST && (data!=null)){
            String faceImagePath = data.getStringExtra("file_path");
            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            firstIv.setImageBitmap(bitmap);
            syncFeature(bitmap,firstFeature,1);
        }else if(requestCode == PICK_VIDEO_SECOND && (data!=null)){
            String faceImagePath = data.getStringExtra("file_path");
            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            secondIv.setImageBitmap(bitmap);
            syncFeature(bitmap,secondFeature,2);
        }
    }

    private void syncFeature(final Bitmap bitmap, final byte[] feature, final int index){
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int type = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL,GlobalFaceTypeModel.RECOGNIZE_LIVE);
                if(type == GlobalFaceTypeModel.RECOGNIZE_LIVE){
                    ret = FaceApi.getInstance().getFeature(bitmap,feature,50);
                }else if(type == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO){
                    ret = FaceApi.getInstance().getFeatureForIDPhoto(bitmap,feature,50);
                }
                Log.i("bshui","ret"+ret);
                if(ret == 512 && index == 1){
                    firstFeatureFinished = true;
                }else if(ret == 512 && index == 2){
                    secondFeatureFinished = true;
                }

                if (ret == 512) {
                    toast("图片" + index + "特征抽取成功");
                } else if (ret == -100) {
                    toast("未完成人脸比对，可能原因，图片1为空");
                } else if (ret == -101) {
                    toast("未完成人脸比对，可能原因，图片2为空");
                } else if (ret == -102) {
                    toast("未完成人脸比对，可能原因，图片1未检测到人脸");
                } else if (ret == -103) {
                    toast("未完成人脸比对，可能原因，图片2未检测到人脸");
                } else {
                    toast("未完成人脸比对，可能原因，"
                            + "人脸太小（小于sdk初始化设置的最小检测人脸）"
                            + "人脸不是朝上，sdk不能检测出人脸");
                }
            }
        });
    }

    private void match(){
        if(!firstFeatureFinished){
            toast("图片1特征没有抽取成功");
            return;
        }

        if(!secondFeatureFinished){
            toast("图片2特征没有抽取成功");
            return;
        }

        float score = 0;
        int type = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL,GlobalFaceTypeModel.RECOGNIZE_LIVE);

        if(type == GlobalFaceTypeModel.RECOGNIZE_LIVE){
            score = FaceApi.getInstance().match(firstFeature,secondFeature);
        }else if(type == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO){
            score = FaceApi.getInstance().matchIDPhoto(firstFeature,secondFeature);

        }
        scoreIv.setText("相似度:"+ score+"%");
    }

    private  void toast(final String tip){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImageMatchImageActivity.this,tip,Toast.LENGTH_LONG).show();
            }
        });
    }
}
